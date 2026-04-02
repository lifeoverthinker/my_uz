package com.example.my_uz_android

import com.example.my_uz_android.util.SubgroupMatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubgroupMatcherTest {

    @Test
    fun `matchesSubgroups should return true when class subgroup is null or blank`() {
        assertTrue(SubgroupMatcher.matchesSubgroups(null, listOf("l1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("", listOf("l1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("   ", listOf("l1")))
    }

    @Test
    fun `matchesSubgroups should return true for common tokens`() {
        assertTrue(SubgroupMatcher.matchesSubgroups("all", listOf("x1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("-", listOf("x1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("brak", listOf("x1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("w", listOf("x1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("wyk", listOf("x1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("L2, wyk, C3", listOf("x1")))
    }

    @Test
    fun `matchesSubgroups should return true when user selection is empty`() {
        assertTrue(SubgroupMatcher.matchesSubgroups("L1, L2, C3", emptyList()))
        assertTrue(SubgroupMatcher.matchesSubgroups("L1, L2, C3", listOf("", "   ")))
    }

    @Test
    fun `matchesSubgroups should match by set intersection for csv values`() {
        assertTrue(SubgroupMatcher.matchesSubgroups("L1, L2, C3", listOf("l1")))
        assertTrue(SubgroupMatcher.matchesSubgroups("L1, L2", listOf("c1, l2")))
        assertTrue(SubgroupMatcher.matchesSubgroups("c3", listOf("x1", " C3 ")))
    }

    @Test
    fun `matchesSubgroups should return false when there is no overlap`() {
        assertFalse(SubgroupMatcher.matchesSubgroups("L1, L2", listOf("c1")))
        assertFalse(SubgroupMatcher.matchesSubgroups("C3", listOf("l1", "l2")))
    }
}

