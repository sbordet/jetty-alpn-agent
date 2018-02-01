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

final class Util {
    static boolean debug;

    static void warn(String message) {
        log(message);
    }

    static void debug(String message) {
        if (debug) {
            log(message);
        }
    }

    private static void log(String message) {
        System.err.println("[jetty-alpn-agent] " + message);
    }

    private Util() {
    }
}
