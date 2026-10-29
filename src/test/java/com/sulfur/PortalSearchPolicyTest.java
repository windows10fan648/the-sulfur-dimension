package com.sulfur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void candidateColumnsAreNineDistinctInteriorPositionsWithCenterFirst() {
        var columns = PortalSearchPolicy.columnsWithinChunk(8, 8);

        assertEquals(9, columns.size());
        assertEquals(new PortalSearchPolicy.ColumnOffset(8, 8), columns.getFirst());
        assertEquals(9, new HashSet<>(columns).size());
        assertTrue(columns.stream().allMatch(column -> column.x() >= 4 && column.x() <= 12
                && column.z() >= 4 && column.z() <= 12));
    }

    @Test
    void exactSourceColumnIsIncludedWithoutIncreasingPerChunkProbeCount() {
        var columns = PortalSearchPolicy.columnsWithinChunk(3, 14);

        assertEquals(9, columns.size());
        assertEquals(new PortalSearchPolicy.ColumnOffset(3, 14), columns.getFirst());
        assertEquals(9, new HashSet<>(columns).size());
        assertTrue(columns.stream().allMatch(column -> column.x() >= 0 && column.x() < 16
                && column.z() >= 0 && column.z() < 16));
    }

    @Test
    void sulfurIsPreferredOnlyWhenEnteringSulfurDimension() {
        assertTrue(PortalSearchPolicy.isPreferred(false, false), "Overworld accepts ordinary dry terrain");
        assertTrue(PortalSearchPolicy.isPreferred(false, true), "Overworld does not require sulfur terrain");
        assertFalse(PortalSearchPolicy.isPreferred(true, false), "Sulfur entry continues looking for sulfur");
        assertTrue(PortalSearchPolicy.isPreferred(true, true), "Sulfur terrain is preferred on entry");
    }

    @Test
    void selectionUsesPreferredSulfurWhenAvailableAndSafeFallbackOtherwise() {
        var ordinary = new Candidate(false);
        var sulfur = new Candidate(true);

        assertEquals(sulfur, PortalSearchPolicy.selectCandidate(java.util.List.of(ordinary, sulfur), Candidate::sulfur));
        assertEquals(ordinary, PortalSearchPolicy.selectCandidate(java.util.List.of(ordinary), Candidate::sulfur));
        assertEquals(ordinary, PortalSearchPolicy.selectCandidate(java.util.List.of(ordinary, sulfur), ignored -> true));
        assertNull(PortalSearchPolicy.selectCandidate(java.util.List.of(), Candidate::sulfur));
    }

    private record Candidate(boolean sulfur) {}
}
