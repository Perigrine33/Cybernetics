package com.perigrine3.createcybernetics.client.navigation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

/** Runs without Minecraft; wired into Gradle check via navigationTerrainGridTest. */
public final class NavigationTerrainGridChecks {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static int color(int x, int z) {
        return 0xFF000000 | ((x + 128) & 255) << 8 | ((z + 128) & 255);
    }

    public static void main(String[] args) {
        var grid = new NavigationTerrainGrid(4);
        var calls = new AtomicInteger();
        IntBinaryOperator sampler = (x, z) -> {
            calls.incrementAndGet();
            return color(x, z);
        };
        grid.moveTo(0, 0);
        grid.update(sampler, 3);
        check(calls.get() == 3, "initial sample budget");
        for (int i = 0; i < 8; i++) grid.update(sampler, 3);
        for (int z = 0; z < 4; z++) {
            for (int x = 0; x < 4; x++) {
                check(grid.color(x, z) == color(x - 2, z - 2), "initial coordinates");
            }
        }
        grid.consumeChanged();
        grid.update(sampler, 0);
        check(!grid.consumeChanged(), "zero budget does no work");
        grid.update(sampler, 16);
        check(!grid.consumeChanged(), "unchanged samples avoid upload");

        grid.moveTo(1, -1);
        check(grid.color(0, 1) == color(-1, -2), "positive X / negative Z reuse");
        check(grid.color(3, 1) == 0, "new east edge unknown");
        for (int i = 0; i < 8; i++) grid.update(sampler, 3);
        for (int z = 0; z < 4; z++) {
            for (int x = 0; x < 4; x++) {
                check(grid.color(x, z) == color(x - 1, z - 3), "shifted coordinates");
            }
        }
        grid.moveTo(-100, 200);
        check(grid.color(0, 0) == 0, "teleport clears stale terrain");
        int before = calls.get();
        grid.update(sampler, 5);
        check(calls.get() - before == 5, "teleport sample budget");

        var unloaded = new NavigationTerrainGrid(4);
        unloaded.moveTo(0, 0);
        int[] attempts = {0};
        boolean[] seen = new boolean[16];
        for (int i = 0; i < 8; i++) {
            int previous = attempts[0];
            unloaded.update((x, z) -> {
                attempts[0]++;
                seen[(z + 2) * 4 + x + 2] = true;
                return 0;
            }, 3);
            check(attempts[0] - previous == 3, "unavailable sample budget");
        }
        for (boolean visited : seen) check(visited, "unloaded retries are fair");
        grid.consumeChanged();
        int old = grid.color(0, 0);
        grid.update((x, z) -> 0, 16);
        check(grid.color(0, 0) == old, "unavailable refresh preserves known terrain");
        checkFrameBudget();
        checkExplorationOrder();
        System.out.println("Passed " + checks + " terrain grid checks");
    }

    private static void checkFrameBudget() {
        var budget = new NavigationFrameBudget(4, 500);
        check(budget.claimWork(), "first tick may process navigation");
        for (int tick = 0; tick < 10; tick++) {
            check(!budget.claimWork(), "catch-up ticks must not multiply navigation work");
        }
        check(budget.claimSample(1_000), "first sample starts time window");
        check(budget.claimSample(1_100), "sample within time budget");
        check(!budget.claimSample(1_500), "deadline stops later samples");
        budget.beginFrame();
        check(budget.claimWork(), "next displayed frame restores one work allowance");
        for (int sample = 0; sample < 4; sample++) {
            check(budget.claimSample(2_000), "sample count allowance");
        }
        check(!budget.claimSample(2_000), "sample count limit");
        check(!budget.claimSample(2_001), "exhausted budget remains exhausted within a frame");
        budget.beginFrame();
        check(budget.claimSample(5_000), "next frame restores sample allowance");
    }

    private static void checkExplorationOrder() {
        var scan = new NavigationExplorationScan(32);
        check(scan.size() == 3209, "exploration coverage retains the full radius-32 circle");
        check(scan.x(0) == 0 && scan.z(0) == 0, "moving player gets explored first");
        var seen = new java.util.HashSet<Long>();
        int previousDistance = -1;
        boolean ordered = true;
        boolean inside = true;
        for (int index = 0; index < scan.size(); index++) {
            int x = scan.x(index);
            int z = scan.z(index);
            int distance = x * x + z * z;
            inside &= distance <= 32 * 32;
            ordered &= distance >= previousDistance;
            previousDistance = distance;
            seen.add(((long) x << 32) ^ (z & 0xFFFFFFFFL));
        }
        check(inside, "exploration stays inside its original range");
        check(ordered, "progressive work always prioritizes nearer terrain");
        check(seen.size() == scan.size(), "each exploration column is visited exactly once");
        var zero = new NavigationExplorationScan(0);
        check(zero.size() == 1 && zero.x(0) == 0 && zero.z(0) == 0, "zero-radius scan");
    }
}
