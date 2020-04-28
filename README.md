# Jetty ALPN Agent

`jetty-alpn-agent` is a JVM agent that enables TLS ALPN (or NPN) extension 
support for Java 7 and 8 by transforming relevant Java classes using the 
correct Jetty `alpn-boot` (or `npn-boot`) JAR file for the JVM version
in use. 

Starting with OpenJDK 1.8.0_252 the use of `jetty-alpn-agent` is not 
strictly required anymore since the standard ALPN APIs have been 
[backported](https://mail.openjdk.java.net/pipermail/jdk8u-dev/2019-November/010573.html)
to OpenJDK 1.8.0_252 from Java 9.

Using `jetty-alpn-agent` with OpenJDK 1.8.0_252 or later performs no
class transformation; the `-javaagent` option can be left in the command
line, but it is encouraged to remove it.
 
Since no class transformation is performed with OpenJDK 1.8.0_252 or later,
the libraries that provide the ALPN implementation must be aware of the
backported standard API and use them to provide the ALPN implementation.

## Usage

Easiest way, that might not to work on all JVMs, is to use the `org.mortbay.jetty.alpn.agent.DynamicAgent`.
Just invoke the `enableJettyAlpnAgent()` method before using secure connections.

## Manually attaching the agent

Specify the path to the agent JAR file with a `-javaagent` option:

```bash
java -javaagent:<path/to/jetty-alpn-agent.jar> ...
```

To use NPN instead of ALPN, specify the `forceNpn=true` option:

```bash
java -javaagent:<path/to/jetty-alpn-agent.jar>=forceNpn=true ...
```

To enable debug logs, specify the `debug=true` option:

```bash
java -javaagent:<path/to/jetty-alpn-agent.jar>=debug=true ...
```

## Using manually attached agent with Maven

Use `maven-dependency-plugin` to fetch the agent JAR and add the path to 
the downloaded agent JAR to the command line arguments:

```xml
<project>
  <properties>
    <jetty.alpnAgent.version>2.0.0</jetty.alpnAgent.version>
    <jetty.alpnAgent.path>${settings.localRepository}/org/mortbay/jetty/alpn/jetty-alpn-agent/${jetty.alpnAgent.version}/jetty-alpn-agent-${jetty.alpnAgent.version}.jar</jetty.alpnAgent.path>
  </properties>
  
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>2.10</version>
        <executions>
          <execution>
            <id>get-jetty-alpn-agent</id>
            <phase>validate</phase>
            <goals>
              <goal>get</goal>
            </goals>
            <configuration>
              <groupId>org.mortbay.jetty.alpn</groupId>
              <artifactId>jetty-alpn-agent</artifactId>
              <version>${jetty.alpnAgent.version}</version>
            </configuration>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>2.19.1</version>
        <configuration>
          <argLine>-javaagent:${jetty.alpnAgent.path}</argLine>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

## Additional resources

For more information, please refer to the following resources:

- Jetty documentation: [ALPN](http://eclipse.org/jetty/documentation/current/alpn-chapter.html) 
and [NPN](http://www.eclipse.org/jetty/documentation/9.2.21.v20170120/npn-chapter.html)
- Java API documentation: [`java.lang.instrument`](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html)

## Legal

This product is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

This product redistributes the original `alpn-boot` and `npn-boot` JARs, which are licensed under [GPLv2 with classpath exception](http://openjdk.java.net/legal/gplv2+ce.html), whose source code is located at:

- https://github.com/jetty-project/jetty-alpn/
- https://github.com/jetty-project/jetty-npn/

## Credits

This library has been initially created by [Trustin Lee](https://github.com/trustin/).
The Jetty Project took ownership in February 2016.
Version 2.0.0 was the first release under the Jetty Project stewardship.
