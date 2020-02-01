# JFlux

![](https://github.com/nickRm/jflux/workflows/CI/badge.svg?branch=develop)

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
jfluxHttpClient.execute("CREATE DATABASE my_db");
```

It is recommended that, if a certain functionality is missing from the `JFluxClient` class, a 
feature request is created so that it can be added.

### Creating a `JFluxClient` instance

To create an instance all you need to do is provide the URL of the InfluxDB host you want to connect
to. The client class, like most classes in this library, uses the 
[builder pattern](https://en.wikipedia.org/wiki/Builder_pattern) for instantiation, so the call 
looks like this:

```java
JFluxClient client = new JFluxClient.Builder("http://localhost:8086").build();
```

The client class implements the `AutoCloseable` interface and it is recommended to close instances 
when you are done using them, so that connections to the InfluxDB API and other resources can be
released. This can easily be done for example using a try-with-resources block:

```java
try (JFluxClient client = new JFluxClient.Builder("http://localhost:8086").build()) {
    // Code that uses the client goes in here. 
    // The client is automatically closed when existing this block.
}
```

### Managing databases with `JFluxClient`

Using the client there are various methods to manage the databases in the connected InfluxDB 
instance:

```java
import com.github.nickrm.jflux.JFluxClient;

class MyApp {

    public static void main(String[] args) {
        try (JFluxClient client = new JFluxClient.Builder("http://localhost:8086").build()) {
            // Check if a database exists.
            boolean exists = client.databaseExists("my_db");
    
            // Get all available databases.
            List<String> databases = client.getDatabases();
    
            // Create a new database.
            client.createDatabase("my_db");
    
            // Drop a database.
            client.dropDatabase("my_db");
        }
    }
}
```

The client also provides a method equivalent to InfluxDB's own `USE DATABASE` statement, which helps
to avoid having to specify the database for every operation (examples of this in practice follow in
the next sections).

### Managing retention policies with `JFluxClient`

Retention policies support is also natively provided by the client:

```java
import java.time.Duration;
import com.github.nickrm.jflux.JFluxClient;
import com.github.nickrm.jflux.domain.RetentionPolicy;

class MyApp {

    public static void main(String[] args) {
        try (JFluxClient client = new JFluxClient().Builder("http://localhost:8086").build()) {
            client.useDatabase("my_db");
        
            // Get all existing retention policies.
            List<RetentionPolicy> existingRetentionPolicies = client.getRetentionPolicies();
    
            // Check if a specific retention policy exists.
            boolean exists = client.retentionPolicyExists("some_rp", "some_other_db");
        
            // Create a new retention policy.
            RetentionPolicy newRetentionPolicy = 
                    new RetentionPolicy.Builder("my_rp", Duration.ofDays(1)).build();
            client.createRetentionPolicy(newRetentionPolicy);
    
            // Get definition of an existing retention policy.
            RetentionPolicy retentionPolicy = client.getRetentionPolicy("my_rp");
        
            // Alter a retention policy.
            client.alterRetentionPolicy("my_rp", 
                    newRetentionPolicy.withShardDuration(Duration.ofHours(1)));
    
            // Drop a retention policy.
            client.dropRetentionPolicy("my_rp");
        }
    }
}
```

### Writing and reading data with `JFluxClient`

Of course, the client also support writing and reading data to and from InfluxDB. This can be done
either using instances of the `Point` class as a more lightweight way, or by using annotations which
the client can use for mapping the field values.

#### Writing and reading points

```java
import java.util.Collections;
import com.github.nickrm.jflux.JFluxClient;
import com.github.nickrm.jflux.domain.Point;

class MyApp {

    public static void main(String[] args) {
        try (JFluxClient client = new JFluxClient.Builder("http://localhost:8086").build()) {
            client.useDatabase("my_db");
    
            // Write points.
            Point point = new Point.Builder()
                .fields(Collections.singletonMap("some_field_name", 1))
                .build();
            client.writePoint("my_measurement", point, "my_retention_policy");
    
            // Read points from a measurement.
            List<Point> points = client.getAllPoints("my_measurement");
        }
    }
}
```

#### Writing and reading annotated objects

```java
import java.time.Instant;
import com.github.nickrm.jflux.JFluxClient;
import com.github.nickrm.jflux.annotation.Field;
import com.github.nickrm.jflux.annotation.Tag;
import com.github.nickrm.jflux.annotation.Timestamp;

class MyApp {

    public static void main(String[] args) {
        try (JFluxClient client = new JFluxClient.Builder("http://localhost:8086").build()) {
            client.useDatabase("my_db");
    
            // Write annotated objects.
            MyAnnotatedObject o = new MyAnnotatedObject();
            o.timestamp = Instant.now();
            o.myTag = "tag_value";
            o.myField = 1;
            client.write(o);
    
            // Read annotated objects.
            List<MyAnnotatedObject> objects = client.getAllPoints(MyAnnotatedObject.class);
        }
    }

    private static class MyAnnotatedObject {
    
        @Timestamp
        Instant timestamp;
    
        @Tag
        String myTag;
    
        @Field
        int myField;
    }
}
```

## Known issues

The client has been tested with InfluxDB OSS 1.7.7 so far.
