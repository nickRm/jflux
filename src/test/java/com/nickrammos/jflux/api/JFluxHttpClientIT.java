package com.nickrammos.jflux.api;

import java.io.IOException;

import com.nickrammos.jflux.domain.Series;
import com.nickrammos.jflux.exception.InvalidQueryException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JFluxHttpClientIT {

    // InfluxDB needs to be running locally for these tests.
    private static final String INFLUX_DB_URL = "http://localhost:8086";

    private static final String DB_NAME = "test_" + System.currentTimeMillis();
    private static final String RP_NAME = "test_retention_policy";

    private JFluxHttpClient client = new JFluxHttpClient.Builder(INFLUX_DB_URL).build();

    @Before
    public void setup() throws IOException {
        client.execute("CREATE DATABASE " + DB_NAME);
    }

    @After
    public void tearDown() throws IOException {
        client.execute("DROP DATABASE " + DB_NAME);
    }

    @Test
    public void testPing() {
        assertThat(client.isConnected()).isTrue();
    }

    @Test
    public void testQuery() throws IOException {
        Series series = client.query("SHOW DATABASES");
        assertThat(series).isNotNull();
    }

    @Test
    public void testQueryWithNoResults() throws IOException {
        Series series = client.query("SHOW MEASUREMENTS ON " + DB_NAME);
        assertThat(series).isNull();
    }

    @Test(expected = InvalidQueryException.class)
    public void testSyntaxError() throws IOException {
        client.query("SHOW DATABASE");
    }

    @Test(expected = InvalidQueryException.class)
    public void testQueryError() throws IOException {
        client.query("SHOW RETENTION POLICIES ON non_existent_db");
    }

    @Test
    public void testStatement() throws IOException {
        // @formatter:off
		String createStatement = "CREATE RETENTION POLICY " + RP_NAME
				+ " ON " + DB_NAME
				+ " DURATION 1d "
				+ " REPLICATION 1";
		// @formatter:on

        client.execute(createStatement);
    }

    @Test(expected = InvalidQueryException.class)
    public void testStatementWithError() throws IOException {
        // Trying to execute incomplete statement.
        client.execute("CREATE RETENTION POLICY");
    }
}
