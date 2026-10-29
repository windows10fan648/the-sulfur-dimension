package com.sulfur;

import java.util.ArrayList;
import java.util.List;

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

    /** Sulfur preference applies only on entry; an ordinary safe surface is always accepted otherwise. */
    static boolean isPreferred(boolean preferSulfur, boolean sulfurSurface) {
        return !preferSulfur || sulfurSurface;
    }

    record ChunkOffset(int x, int z) {}
}
