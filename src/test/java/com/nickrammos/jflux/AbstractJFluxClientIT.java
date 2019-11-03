package com.nickrammos.jflux;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractJFluxClientIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";

    final String dbName = getClass().getSimpleName() + "_" + System.currentTimeMillis();

    JFluxClient jFluxClient;

    @Before
    public void setup() throws IOException {
        jFluxClient = new JFluxClient.Builder(INFLUX_DB_URL).build();
        jFluxClient.createDatabase(dbName);
    }

    @After
    public void tearDown() {
        jFluxClient.dropDatabase(dbName);
    }
}
