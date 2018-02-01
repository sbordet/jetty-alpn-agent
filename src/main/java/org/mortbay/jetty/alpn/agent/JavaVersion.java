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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class JavaVersion {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:_([0-9]+))?(?:-.+)?$");
    private static final int major;
    private static final int minor;
    private static final int micro;
    private static final int patch;

    static {
        final String versionStr = System.getProperty("java.version", "");
        final Matcher m = VERSION_PATTERN.matcher(versionStr);

        if (!m.matches()) {
            Util.warn("Could not parse java.version: " + versionStr);
            major = 0;
            minor = 0;
            micro = 0;
            patch = 0;
        } else {
            major = Integer.parseInt(m.group(1));
            minor = Integer.parseInt(m.group(2));
            micro = Integer.parseInt(m.group(3));
            final String patchStr = m.group(4);
            if (patchStr == null) {
                patch = 0;
            } else {
                patch = Integer.parseInt(patchStr);
            }
        }
    }

    static int major() {
        return major;
    }

    static int minor() {
        return minor;
    }

    static int micro() {
        return micro;
    }

    static int patch() {
        return patch;
    }

    private JavaVersion() {
    }
}
