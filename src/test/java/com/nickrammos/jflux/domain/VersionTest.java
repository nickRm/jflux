package com.nickrammos.jflux.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionTest {

	@Test
	public void ctor_shouldAcceptZeroes() {
		Version version = new Version(0, 0, 0);
		assertThat(version.toString()).isEqualTo("0.0.0");
	}

	@Test(expected = IllegalArgumentException.class)
	public void ctor_shouldThrow_ifMajorNegative() {
		new Version(-1, 0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void ctor_shouldThrow_ifMinorNegative() {
		new Version(1, -1, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void ctor_shouldThrow_ifPathNegative() {
		new Version(1, 0, -1);
	}

	@Test
	public void fromString_shouldParseValidVersion() {
		Version version = Version.fromString("1.0.0");
		assertThat(version.toString()).isEqualTo("1.0.0");
	}

	@Test(expected = NullPointerException.class)
	public void fromString_shouldThrow_ifInputNull() {
		Version.fromString(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fromString_shouldThrow_ifFormatInvalid() {
		Version.fromString("1.0");
	}

	@Test
	public void compareTo_shouldBeConsistentWithEquals() {
		Version v1 = new Version(1, 0, 0);
		Version v2 = new Version(1, 0, 0);

		assertThat(v1).isEqualTo(v2);
		assertThat(v1.compareTo(v2)).isZero();
	}

	@Test
	public void comparTo_shouldCompareMajorVersions() {
		Version v1 = new Version(1, 0, 0);
		Version v2 = new Version(2, 0, 0);
		assertThat(v1.compareTo(v2)).isNegative();
	}

	@Test
	public void comparTo_shouldCompareMinorVersions_ifMajorIsSame() {
		Version v1 = new Version(1, 0, 0);
		Version v2 = new Version(1, 1, 0);
		assertThat(v1.compareTo(v2)).isNegative();
	}

	@Test
	public void comparTo_shouldComparePatchVersions_ifMinorIsSame() {
		Version v1 = new Version(1, 0, 0);
		Version v2 = new Version(1, 0, 1);
		assertThat(v1.compareTo(v2)).isNegative();
	}

	@Test(expected = NullPointerException.class)
	public void compareTo_shouldThrow_ifOtherIsNull() {
		Version version = new Version(1, 0, 0);
		version.compareTo(null);
	}
}
