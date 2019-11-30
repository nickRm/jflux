package com.nickrammos.jflux;

import com.nickrammos.jflux.api.JFluxHttpClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseManagerTest {

    @Mock
    private JFluxHttpClient httpClient;

    private DatabaseManager databaseManager;

    @Before
    public void setup() {
        databaseManager = new DatabaseManager(httpClient);
    }

    @Test
    public void databaseExists_shouldThrowException_ifNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> databaseManager.databaseExists(null));
    }

    @Test
    public void createDatabase_shouldThrowException_ifNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> databaseManager.createDatabase(null));
    }

    @Test
    public void dropDatabase_shouldThrowException_ifNameIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> databaseManager.dropDatabase(null));
    }

    @Test
    public void dropDatabase_shouldThrowException_whenTryingToDropInternalDatabase() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> databaseManager.dropDatabase(DatabaseManager.INTERNAL_DATABASE_NAME));
    }
}
