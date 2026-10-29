package com.sulfur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class PortalConstructionPolicyTest {
    @Test
    void geometryIsA4By5FrameWith2By3InteriorAndZAxisPortalBlocks() {
        var geometry = PortalConstructionPolicy.geometry();

        assertEquals(20, geometry.size());
        assertEquals(14, geometry.stream().filter(PortalConstructionPolicy.PortalCell::frame).count());
        assertEquals(6, geometry.stream().filter(cell -> !cell.frame()).count());
        assertTrue(geometry.stream().allMatch(cell -> cell.offset().z() == 0));
        assertTrue(geometry.stream().filter(cell -> !cell.frame()).allMatch(cell ->
                cell.offset().x() == 1 || cell.offset().x() == 2));
        assertTrue(geometry.stream().filter(cell -> !cell.frame()).allMatch(cell ->
                cell.offset().y() >= 1 && cell.offset().y() <= 3));
    }

    @Test
    void geometryAndPortalStateAxisMatchInBothSupportedOrientations() {
        var widthZ = PortalConstructionPolicy.geometry(PortalConstructionPolicy.Orientation.WIDTH_Z);

        assertEquals(20, widthZ.size());
        assertEquals(14, widthZ.stream().filter(PortalConstructionPolicy.PortalCell::frame).count());
        assertEquals(6, widthZ.stream().filter(cell -> !cell.frame()).count());
        assertTrue(widthZ.stream().allMatch(cell -> cell.offset().x() == 0));
        assertTrue(widthZ.stream().filter(cell -> !cell.frame()).allMatch(cell ->
                cell.offset().z() == 1 || cell.offset().z() == 2));
        assertEquals(PortalConstructionPolicy.Orientation.WIDTH_X,
                PortalConstructionPolicy.orientationForPortalAxis(false));
        assertEquals(PortalConstructionPolicy.Orientation.WIDTH_Z,
                PortalConstructionPolicy.orientationForPortalAxis(true));
    }

    @Test
    void buildPlanCreatesEveryFrameAndInteriorCellOnlyAfterFullPreflight() {
        Map<PortalConstructionPolicy.Offset, PortalConstructionPolicy.ExistingCell> blocks = new HashMap<>();

        var plan = PortalConstructionPolicy.planBuild(offset -> read(blocks, offset), offset -> true).orElseThrow();

        assertEquals(20, plan.size());
        assertEquals(14, plan.stream().filter(p ->
                p.kind() == PortalConstructionPolicy.PlacementKind.SULFUR_FRAME).count());
        assertEquals(6, plan.stream().filter(p ->
                p.kind() == PortalConstructionPolicy.PlacementKind.PORTAL_INTERIOR).count());
    }

    @Test
    void preservesExistingSulfurFrameBlocksAndRejectsOccupiedCellsWithoutPartialPlan() {
        Map<PortalConstructionPolicy.Offset, PortalConstructionPolicy.ExistingCell> blocks = new HashMap<>();
        var frameCell = PortalConstructionPolicy.geometry().stream()
                .filter(PortalConstructionPolicy.PortalCell::frame).findFirst().orElseThrow();
        blocks.put(frameCell.offset(), PortalConstructionPolicy.ExistingCell.SULFUR);

        var plan = PortalConstructionPolicy.planBuild(offset -> read(blocks, offset), offset -> true).orElseThrow();
        assertEquals(19, plan.size());
        assertFalse(plan.stream().anyMatch(p -> p.offset().equals(frameCell.offset())));

        blocks.put(frameCell.offset(), PortalConstructionPolicy.ExistingCell.OCCUPIED);
        assertTrue(PortalConstructionPolicy.planBuild(offset -> read(blocks, offset), offset -> true).isEmpty());
    }

    @Test
    void rejectsFluidOrOccupiedInteriorAndUnsupportedBase() {
        Map<PortalConstructionPolicy.Offset, PortalConstructionPolicy.ExistingCell> blocks = new HashMap<>();
        var interiorCell = PortalConstructionPolicy.geometry().stream()
                .filter(cell -> !cell.frame()).findFirst().orElseThrow();

        blocks.put(interiorCell.offset(), PortalConstructionPolicy.ExistingCell.FLUID);
        assertTrue(PortalConstructionPolicy.planBuild(offset -> read(blocks, offset), offset -> true).isEmpty());

        blocks.put(interiorCell.offset(), PortalConstructionPolicy.ExistingCell.AIR);
        assertTrue(PortalConstructionPolicy.planBuild(offset -> read(blocks, offset),
                offset -> !(offset.x() == 2 && offset.y() == -1)).isEmpty());
    }

    @Test
    void reusesOnlyCompleteCorrectlyOrientedPortalGeometry() {
        Map<PortalConstructionPolicy.Offset, PortalConstructionPolicy.ExistingCell> blocks = new HashMap<>();
        for (var cell : PortalConstructionPolicy.geometry()) {
            blocks.put(cell.offset(), cell.frame()
                    ? PortalConstructionPolicy.ExistingCell.SULFUR
                    : PortalConstructionPolicy.ExistingCell.PORTAL_Z);
        }

        assertTrue(PortalConstructionPolicy.isCompletePortal(offset -> read(blocks, offset)));
        var opening = PortalConstructionPolicy.geometry().stream()
                .filter(cell -> !cell.frame()).findFirst().orElseThrow();
        blocks.put(opening.offset(), PortalConstructionPolicy.ExistingCell.PORTAL_X);
        assertFalse(PortalConstructionPolicy.isCompletePortal(offset -> read(blocks, offset)));
    }

    @Test
    void recognizesACompleteXOrientedPortalAndRejectsTheWrongPortalAxis() {
        Map<PortalConstructionPolicy.Offset, PortalConstructionPolicy.ExistingCell> blocks = new HashMap<>();
        for (var cell : PortalConstructionPolicy.geometry(PortalConstructionPolicy.Orientation.WIDTH_Z)) {
            blocks.put(cell.offset(), cell.frame()
                    ? PortalConstructionPolicy.ExistingCell.SULFUR
                    : PortalConstructionPolicy.ExistingCell.PORTAL_X);
        }

        assertTrue(PortalConstructionPolicy.isCompletePortal(offset -> read(blocks, offset),
                PortalConstructionPolicy.Orientation.WIDTH_Z));
        assertFalse(PortalConstructionPolicy.isCompletePortal(offset -> read(blocks, offset),
                PortalConstructionPolicy.Orientation.WIDTH_X));
    }

    private static PortalConstructionPolicy.ExistingCell read(
            Map<PortalConstructionPolicy.Offset, PortalConstructionPolicy.ExistingCell> blocks,
            PortalConstructionPolicy.Offset offset) {
        return blocks.getOrDefault(offset, PortalConstructionPolicy.ExistingCell.AIR);
    }
}
