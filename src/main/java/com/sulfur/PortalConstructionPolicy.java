package com.sulfur;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/** Pure geometry and validation rules for a 4-by-5 Sulfur portal frame. */
final class PortalConstructionPolicy {
    enum ExistingCell { AIR, SULFUR, PORTAL_X, PORTAL_Z, OCCUPIED, FLUID }
    enum PlacementKind { SULFUR_FRAME, PORTAL_INTERIOR }
    enum Orientation { WIDTH_X, WIDTH_Z }
    record Offset(int x, int y, int z) {}
    record PortalCell(Offset offset, boolean frame) {}
    record Placement(Offset offset, PlacementKind kind) {}

    private static final List<PortalCell> WIDTH_X_GEOMETRY = createGeometry(Orientation.WIDTH_X);
    private static final List<PortalCell> WIDTH_Z_GEOMETRY = createGeometry(Orientation.WIDTH_Z);

    private PortalConstructionPolicy() {}

    static List<PortalCell> geometry() {
        return geometry(Orientation.WIDTH_X);
    }

    static List<PortalCell> geometry(Orientation orientation) {
        return orientation == Orientation.WIDTH_X ? WIDTH_X_GEOMETRY : WIDTH_Z_GEOMETRY;
    }

    static Orientation orientationForPortalAxis(boolean axisX) {
        return axisX ? Orientation.WIDTH_Z : Orientation.WIDTH_X;
    }

    /** A reusable portal must have all 14 frame cells and six correctly oriented interior cells. */
    static boolean isCompletePortal(Function<Offset, ExistingCell> read) {
        return isCompletePortal(read, Orientation.WIDTH_X);
    }

    static boolean isCompletePortal(Function<Offset, ExistingCell> read, Orientation orientation) {
        ExistingCell expectedInterior = orientation == Orientation.WIDTH_X
                ? ExistingCell.PORTAL_Z : ExistingCell.PORTAL_X;
        for (PortalCell cell : geometry(orientation)) {
            ExistingCell existing = read.apply(cell.offset());
            if (cell.frame() ? existing != ExistingCell.SULFUR : existing != expectedInterior) return false;
        }
        return true;
    }

    /**
     * Return a complete placement plan only when every target can be built without replacing
     * existing solid blocks or fluids and every bottom-frame block has safe ground support.
     */
    static Optional<List<Placement>> planBuild(
            Function<Offset, ExistingCell> read, Predicate<Offset> hasSafeSupport) {
        return planBuild(read, hasSafeSupport, Orientation.WIDTH_X);
    }

    static Optional<List<Placement>> planBuild(Function<Offset, ExistingCell> read,
                                                Predicate<Offset> hasSafeSupport,
                                                Orientation orientation) {
        for (int x = 0; x < 4; x++) {
            Offset support = orientation == Orientation.WIDTH_X
                    ? new Offset(x, -1, 0) : new Offset(0, -1, x);
            if (!hasSafeSupport.test(support)) return Optional.empty();
        }
        List<PortalCell> geometry = geometry(orientation);
        List<Placement> plan = new ArrayList<>(geometry.size());
        for (PortalCell cell : geometry) {
            ExistingCell existing = read.apply(cell.offset());
            if (cell.frame()) {
                if (existing == ExistingCell.AIR) {
                    plan.add(new Placement(cell.offset(), PlacementKind.SULFUR_FRAME));
                } else if (existing != ExistingCell.SULFUR) {
                    return Optional.empty();
                }
            } else {
                if (existing != ExistingCell.AIR) return Optional.empty();
                plan.add(new Placement(cell.offset(), PlacementKind.PORTAL_INTERIOR));
            }
        }
        return Optional.of(List.copyOf(plan));
    }

    private static List<PortalCell> createGeometry(Orientation orientation) {
        List<PortalCell> cells = new ArrayList<>(20);
        for (int width = 0; width < 4; width++) {
            for (int y = 0; y < 5; y++) {
                boolean frame = width == 0 || width == 3 || y == 0 || y == 4;
                Offset offset = orientation == Orientation.WIDTH_X
                        ? new Offset(width, y, 0) : new Offset(0, y, width);
                cells.add(new PortalCell(offset, frame));
            }
        }
        return List.copyOf(cells);
    }
}
