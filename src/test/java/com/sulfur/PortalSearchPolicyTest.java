package com.sulfur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class PortalSearchPolicyTest {
    @Test
    void searchPlanIsBoundedDistinctAndCenterFirst() {
        var offsets = PortalSearchPolicy.searchOffsets();

        assertEquals(25, offsets.size());
        assertEquals(new PortalSearchPolicy.ChunkOffset(0, 0), offsets.getFirst());
        assertEquals(25, new HashSet<>(offsets).size());
        assertTrue(offsets.stream().allMatch(offset -> Math.abs(offset.x()) <= 2 && Math.abs(offset.z()) <= 2));
        assertTrue(offsets.stream().anyMatch(offset -> Math.abs(offset.x()) == 2 && Math.abs(offset.z()) == 2));
    }

    @Test
    void searchPlanContainsEveryChunkInTheBoundedFiveByFiveWindow() {
        Set<PortalSearchPolicy.ChunkOffset> expected = new HashSet<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                expected.add(new PortalSearchPolicy.ChunkOffset(x, z));
            }
        }

        assertEquals(expected, new HashSet<>(PortalSearchPolicy.searchOffsets()));
    }

    @Test
    void sulfurIsPreferredOnlyWhenEnteringSulfurDimension() {
        assertTrue(PortalSearchPolicy.isPreferred(false, false), "Overworld accepts ordinary dry terrain");
        assertTrue(PortalSearchPolicy.isPreferred(false, true), "Overworld does not require sulfur terrain");
        assertFalse(PortalSearchPolicy.isPreferred(true, false), "Sulfur entry continues looking for sulfur");
        assertTrue(PortalSearchPolicy.isPreferred(true, true), "Sulfur terrain is preferred on entry");
    }
}
