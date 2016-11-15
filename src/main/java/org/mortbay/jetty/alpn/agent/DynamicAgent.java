/*
 * Copyright 2016 Mikko Tiihonen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mortbay.jetty.alpn.agent;

import static java.lang.System.getProperty;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.newOutputStream;
import static java.util.Arrays.asList;
import static java.util.jar.Attributes.Name.MANIFEST_VERSION;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class DynamicAgent {
    private static final String DYNAMIC_AGENT = "dynamic-agent";

    public static void enableJettyAlpnAgent(String args) {
        attachJavaAgent(Premain.class, args);
    }

    public static void attachJavaAgent(Class<?> agentClass, String args) {
        Path toolsPath = resolveToolsPath();
        String pid = resolvePid();
        Path agentPath = createAgentProxy(agentClass);
        Class<?> virtualMachineClass = loadVirtualMachineClass(toolsPath);
        attachAgent(pid, agentPath, virtualMachineClass, args);
    }

    private static Path resolveToolsPath() {
        Path javaHome = Paths.get(getProperty("java.home"));
        for (Path libPath : asList(Paths.get("lib", "tools.jar"), Paths.get("..", "lib", "tools.jar"))) {
            Path path = javaHome.resolve(libPath);
            if (Files.exists(path)) {
                return path;
            }
        }
        throw new RuntimeException("Could not find tools.jar from java installation at " + javaHome);
    }

    private static String resolvePid() {
        String nameOfRunningVM = getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        if (p < 0) {
            throw new RuntimeException("Could not parse pid from JVM name: " + nameOfRunningVM);
        }
        return nameOfRunningVM.substring(0, p);
    }

    private static Path createAgentProxy(Class<?> agentClass) {
        try {
            Path agentPath = createTempFile(DYNAMIC_AGENT, ".jar");
            agentPath.toFile().deleteOnExit();
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(MANIFEST_VERSION, "1.0");
            manifest.getMainAttributes().putValue("Agent-Class", agentClass.getName());
            try (JarOutputStream out = new JarOutputStream(newOutputStream(agentPath), manifest)) {
                return agentPath;
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create " + DYNAMIC_AGENT + ".jar", e);
        }
    }

    private static Class<?> loadVirtualMachineClass(Path toolsPath) {
        try {
            URL[] urls = new URL[] { toolsPath.toUri().toURL() };
            URLClassLoader loader = new URLClassLoader(urls, null);
            return loader.loadClass("com.sun.tools.attach.VirtualMachine");
        } catch (MalformedURLException|ClassNotFoundException e) {
            throw new RuntimeException("Could not find code for dynamically attaching to JVM", e);
        }
    }

    private static void attachAgent(String pid, Path agentPath, Class<?> virtualMachineClass, String args) {
        try {
            Object virtualMachine = virtualMachineClass.getMethod("attach", String.class).invoke(null, pid);
            try {
                Util.log("Dynamically attaching java agent");
                virtualMachineClass.getMethod("loadAgent", String.class, String.class).invoke(virtualMachine, agentPath.toString(), args);
            } finally {
                virtualMachineClass.getMethod("detach").invoke(virtualMachine);
            }
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException("Could not attach jetty-alpn-agent", e);
        }
    }

    private DynamicAgent() { }
}
