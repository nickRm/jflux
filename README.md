#JFlux

JFlux is a Java client for [InfluxDB](https://www.influxdata.com/products/influxdb-overview/), a 
time series database commonly used for monitoring and other things. The rationale behind the design
of the client was to abstract away a lot of the queries needed for managing an InfluxDB instance,
so to end up with more expressive code instead.

## Usage

Recommended usage is via the `JFluxClient` class. This class abstracts away a lot of the specifics
into reasonable methods. For example creating a database looks like this:

```java
jfluxClient.createDatabase("my_db");
``` 

Conversely there is also a `JFluxHttpClient` which can be used to call the InfluxDB HTTP API 
directly (this is also what `JFluxClient` uses internally). The HTTP client allows more customized
commands to be executed and thus potentially provides more functionality, it is however less 
expressive and can potentially lead to lots of duplication and handling of the lower level parts of 
the communication with InfluxDB.

Creating a database with `JFluxHttpClient` looks like this:

```java
jfluxHttpClient.execut("CREATE DATABASE my_db");
```

It is recommended that, if a certain functionality is missing from the `JFluxClient` class, a 
feature request is created so that it can be added.

## Known issues

The client has been tested with InfluxDB OSS 1.7.7 so far.
