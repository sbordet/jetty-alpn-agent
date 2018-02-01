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

final class VersionMapping {
    private final String artifactVersion;
    private final int major;
    private final int minor;
    private final int micro;
    private final int startPatch;

    VersionMapping(String artifactVersion, int major, int minor, int micro, int startPatch) {
        this.artifactVersion = artifactVersion;
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.startPatch = startPatch;
    }

    String artifactVersion() {
        return artifactVersion;
    }

    boolean matches() {
        if (JavaVersion.major() != major) {
            return false;
        }

        if (JavaVersion.minor() != minor) {
            return false;
        }

        if (JavaVersion.micro() != micro) {
            return false;
        }

        return JavaVersion.patch() >= startPatch;
    }

    @Override
    public String toString() {
        return artifactVersion + ": " + major + '.' + minor + '.' + micro + '_' + startPatch + '~';
    }
}
