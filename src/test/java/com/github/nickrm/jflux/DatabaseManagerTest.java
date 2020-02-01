package com.github.nickrm.jflux;

import com.github.nickrm.jflux.api.JFluxHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@ExtendWith(MockitoExtension.class)
public class DatabaseManagerTest {

    @Mock
    private JFluxHttpClient httpClient;

    private DatabaseManager databaseManager;

    @BeforeEach
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
