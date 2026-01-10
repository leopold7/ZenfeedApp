package com.ddyy.zenfeed

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

data class VersionInfo(
    val coreVersion: List<Int>,
    val isDev: Boolean,
    val timestamp: Long?
)

fun parseVersion(version: String): VersionInfo {
    val trimmedVersion = version.trim()
    
    val devPattern = Regex("^(\\d+(?:\\.\\d+)*)\\.dev\\.(\\d+)$")
    val devMatch = devPattern.find(trimmedVersion)
    
    if (devMatch != null) {
        val coreVersion = devMatch.groupValues[1].split(".").map { it.toInt() }
        val timestamp = devMatch.groupValues[2].toLong()
        return VersionInfo(coreVersion, true, timestamp)
    }
    
    val coreVersion = trimmedVersion.split(".").map { it.toInt() }
    return VersionInfo(coreVersion, false, null)
}

fun compareVersions(v1: String, v2: String): Int {
    val version1 = parseVersion(v1)
    val version2 = parseVersion(v2)
    
    val maxParts = maxOf(version1.coreVersion.size, version2.coreVersion.size)
    
    for (i in 0 until maxParts) {
        val part1 = version1.coreVersion.getOrElse(i) { 0 }
        val part2 = version2.coreVersion.getOrElse(i) { 0 }
        
        if (part1 > part2) return 1
        if (part1 < part2) return -1
    }
    
    if (!version1.isDev && version2.isDev) return 1
    if (version1.isDev && !version2.isDev) return -1
    
    if (version1.isDev && version2.isDev) {
        val timestamp1 = version1.timestamp ?: 0
        val timestamp2 = version2.timestamp ?: 0
        
        if (timestamp1 > timestamp2) return 1
        if (timestamp1 < timestamp2) return -1
    }
    
    return 0
}

fun isNewerVersion(latestVersion: String, currentVersion: String): Boolean {
    return compareVersions(latestVersion, currentVersion) > 0
}

data class ReleaseInfo(
    val tagName: String,
    val isDev: Boolean
)

data class UpdateCheckResult(
    val hasUpdate: Boolean,
    val latestVersion: String?,
    val reason: String
)

fun checkForUpdateLogic(
    currentVersion: String,
    branch: String,
    latestDevVersion: String?,
    latestMasterVersion: String?
): UpdateCheckResult {
    val targetVersion: String? = if (branch == "dev") {
        if (latestDevVersion != null) {
            if (latestMasterVersion != null) {
                if (compareVersions(latestMasterVersion, latestDevVersion) > 0) {
                    latestMasterVersion
                } else {
                    latestDevVersion
                }
            } else {
                latestDevVersion
            }
        } else {
            latestMasterVersion
        }
    } else {
        latestMasterVersion
    }
    
    if (targetVersion == null) {
        return UpdateCheckResult(false, null, "No release found for branch: $branch")
    }
    
    val hasUpdate = isNewerVersion(targetVersion, currentVersion)
    
    return UpdateCheckResult(
        hasUpdate = hasUpdate,
        latestVersion = if (hasUpdate) targetVersion else null,
        reason = if (hasUpdate) {
            "New version available: $targetVersion > $currentVersion"
        } else {
            "Already on latest version: $currentVersion >= $targetVersion"
        }
    )
}

class VersionComparisonTest {
    
    @Test
    fun testStableVersionGreaterThanDevVersionWithSameCore() {
        val result = compareVersions("1.6.7", "1.6.7.dev.20260109201010")
        assertTrue("1.6.7 should be greater than 1.6.7.dev.20260109201010", result > 0)
    }
    
    @Test
    fun testDevVersionWithNewerTimestamp() {
        val result = compareVersions("1.6.7.dev.20260109201010", "1.6.7.dev.20260108201010")
        assertTrue("1.6.7.dev.20260109201010 should be greater than 1.6.7.dev.20260108201010", result > 0)
    }
    
    @Test
    fun testDevVersionWithHigherCoreVersionGreaterThanStableVersion() {
        val result = compareVersions("1.6.7.dev.20260109201010", "1.6.6")
        assertTrue("1.6.7.dev.20260109201010 should be greater than 1.6.6", result > 0)
    }
    
    @Test
    fun testStableVersionWithHigherCoreVersion() {
        val result = compareVersions("1.6.7", "1.6.6")
        assertTrue("1.6.7 should be greater than 1.6.6", result > 0)
    }
    
    @Test
    fun testEqualStableVersions() {
        val result = compareVersions("1.6.7", "1.6.7")
        assertEquals("1.6.7 should be equal to 1.6.7", 0, result)
    }
    
    @Test
    fun testEqualDevVersions() {
        val result = compareVersions("1.6.7.dev.20260109201010", "1.6.7.dev.20260109201010")
        assertEquals("1.6.7.dev.20260109201010 should be equal to 1.6.7.dev.20260109201010", 0, result)
    }
    
    @Test
    fun testDevVersionWithLowerCoreVersionLessThanStableVersion() {
        val result = compareVersions("1.6.5.dev.20260103201010", "1.6.6")
        assertTrue("1.6.5.dev.20260103201010 should be less than 1.6.6", result < 0)
    }
    
    @Test
    fun testDevVersionWithHigherCoreVersionGreaterThanStableVersion2() {
        val result = compareVersions("1.6.8.dev.20260110201010", "1.6.7")
        assertTrue("1.6.8.dev.20260110201010 should be greater than 1.6.7", result > 0)
    }
    
    @Test
    fun testStableVersionGreaterThanDevVersionWithSameCore2() {
        val result = compareVersions("1.6.7", "1.6.7.dev.20260109201010")
        assertTrue("1.6.7 should be greater than 1.6.7.dev.20260109201010", result > 0)
    }
    
    @Test
    fun testStableVersionWithLowerCoreVersionLessThanDevVersion() {
        val result = compareVersions("1.6.7", "1.6.8.dev.20260110201010")
        assertTrue("1.6.7 should be less than 1.6.8.dev.20260110201010", result < 0)
    }
    
    @Test
    fun testVersionSorting() {
        val versions = listOf(
            "1.6.7",
            "1.6.7.dev.20260109201010",
            "1.6.7.dev.20260108201010",
            "1.6.6",
            "1.6.5.dev.20260103201010"
        )
        
        val sortedVersions = versions.sortedWith { v1, v2 -> -compareVersions(v1, v2) }
        
        val expected = listOf(
            "1.6.7",
            "1.6.7.dev.20260109201010",
            "1.6.7.dev.20260108201010",
            "1.6.6",
            "1.6.5.dev.20260103201010"
        )
        
        assertEquals("Version sorting should match expected order", expected, sortedVersions)
    }
    
    @Test
    fun testIsNewerVersion() {
        assertTrue("1.6.7 should be newer than 1.6.6", isNewerVersion("1.6.7", "1.6.6"))
        assertTrue("1.6.7.dev.20260109201010 should be newer than 1.6.7.dev.20260108201010", 
            isNewerVersion("1.6.7.dev.20260109201010", "1.6.7.dev.20260108201010"))
        assertTrue("1.6.7.dev.20260109201010 should be newer than 1.6.6", 
            isNewerVersion("1.6.7.dev.20260109201010", "1.6.6"))
    }
    
    // ========== 测试 checkForUpdate 逻辑 ==========
    
    @Test
    fun testCheckForUpdate_DevBranch_NewerDevVersionAvailable() {
        val currentVersion = "1.6.7.dev.20260108201010"
        val latestDevVersion = "1.6.7.dev.20260109201010"
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "dev", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when master is newer than dev", result.hasUpdate)
        assertEquals("Latest version should be master version", latestMasterVersion, result.latestVersion)
        println("Test: Dev branch with newer dev version - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_DevBranch_NoNewerDevVersion() {
        val currentVersion = "1.6.7.dev.20260109201010"
        val latestDevVersion = "1.6.7.dev.20260108201010"
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "dev", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when master is newer than current dev", result.hasUpdate)
        assertEquals("Latest version should be master version", latestMasterVersion, result.latestVersion)
        println("Test: Dev branch with latest dev version - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_DevBranch_NewerCoreVersionDevAvailable() {
        val currentVersion = "1.6.7.dev.20260109201010"
        val latestDevVersion = "1.6.8.dev.20260110201010"
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "dev", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when dev with higher core version available", result.hasUpdate)
        assertEquals("Latest version should be $latestDevVersion", latestDevVersion, result.latestVersion)
        println("Test: Dev branch with higher core version dev - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_MasterBranch_NewerMasterVersionAvailable() {
        val currentVersion = "1.6.6"
        val latestDevVersion = "1.6.7.dev.20260109201010"
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "master", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when newer master version available", result.hasUpdate)
        assertEquals("Latest version should be $latestMasterVersion", latestMasterVersion, result.latestVersion)
        println("Test: Master branch with newer master version - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_MasterBranch_NoNewerMasterVersion() {
        val currentVersion = "1.6.7"
        val latestDevVersion = "1.6.7.dev.20260109201010"
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "master", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should not have update when current master is latest", !result.hasUpdate)
        assertNull("Latest version should be null", result.latestVersion)
        println("Test: Master branch with latest master version - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_MasterBranch_CurrentDevVersion() {
        val currentVersion = "1.6.7.dev.20260109201010"
        val latestDevVersion = "1.6.7.dev.20260110201010"
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "master", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when master is newer than current dev", result.hasUpdate)
        assertEquals("Latest version should be $latestMasterVersion", latestMasterVersion, result.latestVersion)
        println("Test: Master branch with current dev version - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_MasterBranch_NewerMasterThanCurrentDev() {
        val currentVersion = "1.6.7.dev.20260109201010"
        val latestDevVersion = "1.6.7.dev.20260110201010"
        val latestMasterVersion = "1.6.8"
        
        val result = checkForUpdateLogic(currentVersion, "master", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when master is newer than current dev", result.hasUpdate)
        assertEquals("Latest version should be $latestMasterVersion", latestMasterVersion, result.latestVersion)
        println("Test: Master branch with master newer than current dev - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_DevBranch_NoDevRelease() {
        val currentVersion = "1.6.7.dev.20260109201010"
        val latestDevVersion = null
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "dev", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when master is newer than current dev", result.hasUpdate)
        assertEquals("Latest version should be $latestMasterVersion", latestMasterVersion, result.latestVersion)
        println("Test: Dev branch with no dev release - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_MasterBranch_NoMasterRelease() {
        val currentVersion = "1.6.7"
        val latestDevVersion = "1.6.7.dev.20260109201010"
        val latestMasterVersion = null
        
        val result = checkForUpdateLogic(currentVersion, "master", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should not have update when no master release available", !result.hasUpdate)
        assertNull("Latest version should be null", result.latestVersion)
        println("Test: Master branch with no master release - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_DevBranch_StableVersionIsBetterThanDev() {
        val currentVersion = "1.6.6"
        val latestDevVersion = "1.6.7.dev.20260109201010"
        val latestMasterVersion = "1.6.7"
        
        val result = checkForUpdateLogic(currentVersion, "dev", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update and choose stable version over dev version", result.hasUpdate)
        assertEquals("Latest version should be stable version", latestMasterVersion, result.latestVersion)
        println("Test: Dev branch with stable version better than dev - ${result.reason}")
    }
    
    @Test
    fun testCheckForUpdate_DevBranch_StableCurrent_NewerDevAvailable() {
        val currentVersion = "1.6.6"
        val latestDevVersion = "1.6.7.dev.20260109201010"
        val latestMasterVersion = "1.6.6"
        
        val result = checkForUpdateLogic(currentVersion, "dev", latestDevVersion, latestMasterVersion)
        
        assertTrue("Should have update when newer dev available for stable current", result.hasUpdate)
        assertEquals("Latest version should be $latestDevVersion", latestDevVersion, result.latestVersion)
        println("Test: Dev branch with stable current and newer dev - ${result.reason}")
    }
}
