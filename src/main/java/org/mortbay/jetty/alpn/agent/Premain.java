/*
 * Copyright 2015 Trustin Heuiseung Lee.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public final class Premain {
    private static final VersionMapping[] ALPN_MAPPINGS = {
            new VersionMapping("8.1.12.v20180117", 1, 8, 0, 161),
            new VersionMapping("8.1.11.v20170118", 1, 8, 0, 121),
            new VersionMapping("8.1.10.v20161026", 1, 8, 0, 112),
            new VersionMapping("8.1.9.v20160720", 1, 8, 0, 101),
            new VersionMapping("8.1.8.v20160420", 1, 8, 0, 92),
            new VersionMapping("8.1.7.v20160121", 1, 8, 0, 71),
            new VersionMapping("8.1.6.v20151105", 1, 8, 0, 65),
            new VersionMapping("8.1.5.v20150921", 1, 8, 0, 60),
            new VersionMapping("8.1.4.v20150727", 1, 8, 0, 51),
            new VersionMapping("8.1.3.v20150130", 1, 8, 0, 31),
            new VersionMapping("8.1.2.v20141202", 1, 8, 0, 25),
            new VersionMapping("8.1.0.v20141016", 1, 8, 0, 0),
            new VersionMapping("7.1.3.v20150130", 1, 7, 0, 75),
            new VersionMapping("7.1.2.v20141202", 1, 7, 0, 71),
            new VersionMapping("7.1.0.v20141016", 1, 7, 0, 0),
    };
    private static final VersionMapping[] NPN_MAPPINGS = {
            new VersionMapping("1.1.11.v20150415", 1, 7, 0, 80),
            new VersionMapping("1.1.10.v20150130", 1, 7, 0, 75),
            new VersionMapping("1.1.9.v20141016", 1, 7, 0, 71),
            new VersionMapping("1.1.8.v20141013", 1, 7, 0, 55),
            new VersionMapping("1.1.6.v20130911", 1, 7, 0, 40),
            new VersionMapping("1.1.5.v20130313", 1, 7, 0, 15),
            new VersionMapping("1.1.4.v20130313", 1, 7, 0, 13),
            new VersionMapping("1.1.3.v20130313", 1, 7, 0, 9),
            new VersionMapping("1.1.1.v20121030", 1, 7, 0, 6),
            new VersionMapping("1.1.0.v20120525", 1, 7, 0, 4),
            new VersionMapping("1.0.0.v20120402", 1, 7, 0, 0)
    };

    public static void agentmain(String options, Instrumentation inst) throws Exception {
        premain(options, inst);
    }
  
    public static void premain(String options, Instrumentation inst) throws Exception {
        String artifactName = "alpn-boot";
        VersionMapping[] mappings = ALPN_MAPPINGS;

        String[] args = options == null ? new String[0] : options.split(",");
        for (String arg : args) {
            arg = arg.trim();
            if (arg.equals("forceNpn=true")) {
                artifactName = "npn-boot";
                mappings = NPN_MAPPINGS;
            } else if (arg.equals("debug=true")) {
                Util.debug = true;
            }
        }

        // Find the matching alpn/npn-boot version.
        final String artifactVersion = findArtifactVersion(mappings);
        if (artifactVersion == null) {
            final String javaVersion = System.getProperty("java.version", "");
            Util.warn("Could not find a matching " + artifactName + " JAR for Java version: " + javaVersion);
            return;
        }

        // Find the URL of the alpn/npn-boot-<version>.jar file.
        final String artifactFileName = artifactName + '-' + artifactVersion + ".jar";
        final URL artifactUrl = Premain.class.getResource(artifactFileName);
        if (artifactUrl == null) {
            Util.warn("Could not find a JAR file: " + artifactFileName);
            return;
        }

        Util.debug("Using: " + artifactFileName);
        configureBootstrapClassLoaderSearch(inst, artifactUrl, artifactName, artifactVersion);
        configureClassFileTransformer(inst, artifactUrl);
    }

    private static String findArtifactVersion(VersionMapping[] mappings) {
        for (VersionMapping m : mappings) {
            if (m.matches()) {
                return m.artifactVersion();
            }
        }
        return null;
    }

    private static void configureBootstrapClassLoaderSearch(Instrumentation inst, URL artifactUrl, String artifactName, String artifactVersion) throws IOException {
        final File tmpFile = File.createTempFile(artifactName + '-' + artifactVersion + '.', ".jar");
        tmpFile.deleteOnExit();

        try (OutputStream out = new FileOutputStream(tmpFile)) {
            try (InputStream in = artifactUrl.openStream()) {
                final byte[] buf = new byte[8192];
                while (true) {
                    final int readBytes = in.read(buf);
                    if (readBytes < 0) {
                        break;
                    }
                    out.write(buf, 0, readBytes);
                }
            }
        }

        inst.appendToBootstrapClassLoaderSearch(new JarFile(tmpFile));
    }

    private static void configureClassFileTransformer(Instrumentation inst, URL artifactUrl) throws IOException {
        final Map<String, byte[]> classes = new HashMap<>();
        try (JarInputStream jarIn = new JarInputStream(artifactUrl.openStream())) {
            final byte[] buf = new byte[8192];
            while (true) {
                final JarEntry e = jarIn.getNextJarEntry();
                if (e == null) {
                    break;
                }

                final String entryName = e.getName();
                if (!entryName.endsWith(".class")) {
                    continue;
                }

                final ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
                while (true) {
                    final int readBytes = jarIn.read(buf);
                    if (readBytes < 0) {
                        break;
                    }
                    out.write(buf, 0, readBytes);
                }

                final String className = entryName.substring(0, entryName.length() - 6);
                classes.put(className, out.toByteArray());
            }
        }
        inst.addTransformer(new ReplacingClassFileTransformer(classes));
    }

    private Premain() {
    }
}
