package com.sulfur;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/** Pure search rules shared by the portal implementation and unit tests. */
final class PortalSearchPolicy {
    static final int MAX_CHUNKS = 25;
    private static final int CHUNK_RADIUS = 2;

    private PortalSearchPolicy() {}

    /** Returns a center-first, expanding-square list of distinct relative chunk offsets. */
    static List<ChunkOffset> searchOffsets() {
        List<ChunkOffset> offsets = new ArrayList<>(MAX_CHUNKS);
        for (int radius = 0; radius <= CHUNK_RADIUS && offsets.size() < MAX_CHUNKS; radius++) {
            for (int dx = -radius; dx <= radius && offsets.size() < MAX_CHUNKS; dx++) {
                for (int dz = -radius; dz <= radius && offsets.size() < MAX_CHUNKS; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    offsets.add(new ChunkOffset(dx, dz));
                }
            }
        }
        return List.copyOf(offsets);
    }

    /** Nine interior sample columns, center first, all remaining within the loaded chunk. */
    static List<ColumnOffset> columnsWithinChunk(int anchorX, int anchorZ) {
        List<ColumnOffset> columns = new ArrayList<>(9);
        columns.add(new ColumnOffset(anchorX, anchorZ));
        columns.add(new ColumnOffset(8, 8));
        for (int x : new int[] {4, 8, 12}) {
            for (int z : new int[] {4, 8, 12}) {
                ColumnOffset column = new ColumnOffset(x, z);
                if (columns.size() < 9 && !columns.contains(column)) columns.add(column);
            }
        }
        return List.copyOf(columns);
    }

    /** Sulfur preference applies only on entry; an ordinary safe surface is always accepted otherwise. */
    static boolean isPreferred(boolean preferSulfur, boolean sulfurSurface) {
        return !preferSulfur || sulfurSurface;
    }

    /** All inputs are already safe; return the first preferred candidate or a safe fallback. */
    static <T> T selectCandidate(List<T> safeCandidates, Predicate<T> preferred) {
        if (safeCandidates.isEmpty()) return null;
        for (T candidate : safeCandidates) {
            if (preferred.test(candidate)) return candidate;
        }
        return safeCandidates.getFirst();
    }

    record ChunkOffset(int x, int z) {}
    record ColumnOffset(int x, int z) {}
}
