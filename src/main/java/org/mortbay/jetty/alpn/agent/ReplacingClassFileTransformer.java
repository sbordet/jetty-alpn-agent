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

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Map;

final class ReplacingClassFileTransformer implements ClassFileTransformer {
    private final Map<String, byte[]> classes;

    ReplacingClassFileTransformer(Map<String, byte[]> classes) {
        this.classes = classes;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        final byte[] content = classes.get(className);
        if (content == null || Arrays.equals(content, classfileBuffer)) {
            return null;
        }

        Util.debug("Replacing: " + className);
        return content.clone();
    }
}
