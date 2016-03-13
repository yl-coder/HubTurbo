package tests;

import org.junit.Test;
import ui.UI;
import util.JavaVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class JavaVersionTest {
    @Test
    public void javaVersionParsing_RequiredVersionString_NoExceptionThrown() {
        JavaVersion.fromString(UI.REQUIRED_JAVA_VERSION);
    }

    @Test
    public void javaVersionParsing_AcceptableVersionString_ParsedVersionCorrectly() {
        // Java version no build
        verifyJavaVersionParsedCorrectly("1.8.0_60", 1, 8, 0, 60, 0);
        // Java version with build
        verifyJavaVersionParsedCorrectly("1.8.0_60-b40", 1, 8, 0, 60, 40);
        // Arbitrary big version numbers no build
        verifyJavaVersionParsedCorrectly("10.80.100_60", 10, 80, 100, 60, 0);
        // Arbitrary big version numbers with build
        verifyJavaVersionParsedCorrectly("010.080.100_60", 10, 80, 100, 60, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaVersionParsing_JavaVersionNoUpdateNoBuild_ThrowsException() {
        JavaVersion.fromString("1.8.0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void javaVersionParsing_RandomString_ThrowsException() {
        JavaVersion.fromString("this should throw exception");
    }

    @Test
    public void javaVersionToString_JavaVersion_ReturnsCorrectString() {
        JavaVersion version = new JavaVersion(0, 0, 0, 0, 0);
        String versionString = "0.0.0_0-b0";
        assertEquals(versionString, version.toString());

        version = new JavaVersion(100, 200, 300, 400, 500);
        versionString = "100.200.300_400-b500";
        assertEquals(versionString, version.toString());
    }

    @Test
    public void javaVersionComparable_JavaVersion_HashCodesAndEqualAreCorrect() {
        String version = "1.8.0_60";
        JavaVersion expectedVersion = new JavaVersion(1, 8, 0, 60, 0);
        JavaVersion javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion.hashCode(), javaVersion.hashCode());
        assertTrue(expectedVersion.equals(javaVersion));

        version = "9.8.0_60-b10";
        expectedVersion = new JavaVersion(9, 8, 0, 60, 10);
        javaVersion = JavaVersion.fromString(version);
        assertEquals(expectedVersion.hashCode(), javaVersion.hashCode());
        assertTrue(expectedVersion.equals(javaVersion));
    }

    @Test
    public void javaVersionTooLow_JavaVersion_ReturnsCorrectResult() {
        JavaVersion age0 = new JavaVersion(0, 0, 0, 0, 0);
        JavaVersion ageBuild1 = new JavaVersion(0, 0, 0, 0, 1);
        JavaVersion ageUpdate1Build0  = new JavaVersion(0, 0, 0, 1, 0);
        JavaVersion ageUpdate1Build1  = new JavaVersion(0, 0, 0, 1, 1);
        JavaVersion ageMinor1 = new JavaVersion(0, 0, 1, 0, 0);
        JavaVersion ageMajor1 = new JavaVersion(0, 1, 0, 0, 0);
        JavaVersion ageDiscard2 = new JavaVersion(2, 0, 0, 0, 0);

        // Case version too low
        assertTrue(JavaVersion.isJavaVersionLower(age0, ageBuild1));
        assertTrue(JavaVersion.isJavaVersionLower(ageBuild1, ageUpdate1Build0));
        assertTrue(JavaVersion.isJavaVersionLower(ageUpdate1Build0, ageUpdate1Build1));
        assertTrue(JavaVersion.isJavaVersionLower(ageUpdate1Build1, ageMinor1));
        assertTrue(JavaVersion.isJavaVersionLower(ageUpdate1Build1, ageMajor1));
        assertTrue(JavaVersion.isJavaVersionLower(ageMinor1, ageMajor1));
        assertTrue(JavaVersion.isJavaVersionLower(ageMajor1, ageDiscard2));

        // Case equal
        assertFalse(JavaVersion.isJavaVersionLower(age0, age0));
        assertFalse(JavaVersion.isJavaVersionLower(ageBuild1, ageBuild1));

        // Case version higher
        assertFalse(JavaVersion.isJavaVersionLower(ageUpdate1Build0, ageBuild1));
        assertFalse(JavaVersion.isJavaVersionLower(ageDiscard2, ageMajor1));
    }

    private void verifyJavaVersionParsedCorrectly(String versionString, int verDiscard, int verMajor, int verMinor,
                                                  int verUpdate, int verBuild) {
        JavaVersion expectedVersion = new JavaVersion(verDiscard, verMajor, verMinor, verUpdate, verBuild);
        JavaVersion javaVersion = JavaVersion.fromString(versionString);
        assertEquals(expectedVersion, javaVersion);
    }
}