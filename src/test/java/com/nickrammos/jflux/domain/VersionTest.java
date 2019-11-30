package com.nickrammos.jflux.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class VersionTest {

    @Test
    public void ctor_shouldAcceptZeroes() {
        Version version = new Version(0, 0, 0);
        assertThat(version.toString()).isEqualTo("0.0.0");
    }

    @Test
    public void ctor_shouldThrow_ifMajorNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Version(-1, 0, 0));
    }

    @Test
    public void ctor_shouldThrow_ifMinorNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Version(1, -1, 0));
    }

    @Test
    public void ctor_shouldThrow_ifPathNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Version(1, 0, -1));
    }

    @Test
    public void fromString_shouldParseValidVersion() {
        Version version = Version.fromString("1.0.0");
        assertThat(version.toString()).isEqualTo("1.0.0");
    }

    @Test
    public void fromString_shouldThrow_ifInputNull() {
        assertThatNullPointerException().isThrownBy(() -> Version.fromString(null));
    }

    @Test
    public void fromString_shouldThrow_ifFormatInvalid() {
        assertThatIllegalArgumentException().isThrownBy(() -> Version.fromString("1.0"));
    }

    @Test
    public void compareTo_shouldBeConsistentWithEquals() {
        Version v1 = new Version(1, 0, 0);
        Version v2 = new Version(1, 0, 0);

        assertThat(v1).isEqualTo(v2);
        assertThat(v1.compareTo(v2)).isZero();
    }

    @Test
    public void compareTo_shouldCompareMajorVersions() {
        Version v1 = new Version(1, 0, 0);
        Version v2 = new Version(2, 0, 0);
        assertThat(v1.compareTo(v2)).isNegative();
    }

    @Test
    public void compareTo_shouldCompareMinorVersions_ifMajorIsSame() {
        Version v1 = new Version(1, 0, 0);
        Version v2 = new Version(1, 1, 0);
        assertThat(v1.compareTo(v2)).isNegative();
    }

    @Test
    public void compareTo_shouldComparePatchVersions_ifMinorIsSame() {
        Version v1 = new Version(1, 0, 0);
        Version v2 = new Version(1, 0, 1);
        assertThat(v1.compareTo(v2)).isNegative();
    }

    @Test
    public void compareTo_shouldThrow_ifOtherIsNull() {
        Version version = new Version(1, 0, 0);
        assertThatNullPointerException().isThrownBy(() -> version.compareTo(null));
    }
}
