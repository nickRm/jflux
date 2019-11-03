package com.nickrammos.jflux;

import com.nickrammos.jflux.api.JFluxHttpClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseManagerTest {

    @Mock
    private JFluxHttpClient httpClient;

    private DatabaseManager databaseManager;

    @Before
    public void setup() {
        databaseManager = new DatabaseManager(httpClient);
    }

    @Test(expected = NullPointerException.class)
    public void databaseExists_shouldThrowException_ifNameIsNull() {
        databaseManager.databaseExists(null);
    }

    @Test(expected = NullPointerException.class)
    public void createDatabase_shouldThrowException_ifNameIsNull() {
        databaseManager.createDatabase(null);
    }

    @Test(expected = NullPointerException.class)
    public void dropDatabase_shouldThrowException_ifNameIsNull() {
        databaseManager.dropDatabase(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropDatabase_shouldThrowException_whenTryingToDropInternalDatabase() {
        databaseManager.dropDatabase(DatabaseManager.INTERNAL_DATABASE_NAME);
    }
}
