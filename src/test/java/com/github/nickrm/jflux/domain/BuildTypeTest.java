package com.github.nickrm.jflux.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class BuildTypeTest {

    @Test
    public void fromIdentifier_shouldReturnBuildType() {
        for (BuildType buildType : BuildType.values()) {
            BuildType result = BuildType.fromIdentifier(buildType.getIdentifier());
            assertThat(result).isEqualTo(buildType);
        }
    }

    @Test
    public void fromIdentifier_shouldThrowException_ifIdentifierIsInvalid() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> BuildType.fromIdentifier("invalid-identifier"));
    }
}
