# GeoserverTomcatAccessLogValve

This custom valve extends on the normal Tomcat AccessLogValve and adds functionality that is able to write the GeoServer user in the access log valve.

The implementation should be compatible with any version of Tomcat 8.5.x and probably works with other versions as well.

## Installation

Add the jar file in the `lib/` folder within your Apache Tomcat instance.

## Configuration

In addition to the pattern codes supported by the default [AccessLogValve implementation](https://tomcat.apache.org/tomcat-8.0-doc/config/valve.html), this valve supports the following additional patterns:
* `%{geoserver-user}G` - writes the user logged in to GeoServer (or '-')
* `%{geoserver-or-normal-user}G` - as above but if not logged in GeoServer, behave as `%u`
The valve supports all pattern codes that are supported by Tomcat by default. In addition to those,  

In your server.xml, replace your access log valve className with `org.geoserver.tomcat.GeoserverTomcatAccessLogValve`. For example:

```xml
<Valve className="org.geoserver.tomcat.GeoserverTomcatAccessLogValve" directory="logs" prefix="localhost_access_log" suffix=".txt" pattern="%h %l %{geoserver-user}G %t &quot;%r&quot; %s %b" />
```

Note that the pattern above is the default configuration from Tomcat except `%u` is replaced by `%{geoserver-user}G`
