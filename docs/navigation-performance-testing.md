# Navigation performance regression checks

Run `gradlew.bat build --offline`. The `navigationTerrainGridTest` task runs 92 checks of sampling limits, catch-up tick handling, sample deadlines, nearest-first exploration order, fair retries for unavailable terrain, movement in both axes, teleports, unchanged texture detection, and preservation of known colors when samples are unavailable. It needs no Minecraft client or test framework.

## In-game checks

Use the updated build on the client and server. Test in an already generated area first, then repeat while generating new chunks. Keep render distance, simulation distance, FPS limit, and the route the same between comparisons.

- Move, sprint, and fly with the minimap enabled. Compare camera smoothness with it disabled. Unavailable terrain should fill progressively; it must not become permanently marked as explored.
- Teleport and change dimensions. Old terrain must disappear from the minimap, then the new area must fill in.
- Stand still near terrain changes and verify that colors eventually refresh. Minimap terrain refresh is deliberately progressive.
- Open the fullscreen map, zoom to the minimum, and pan across explored and empty areas. New tile textures are uploaded at most four per frame; a large view fills progressively. Moving away releases distant textures.
- Right-click empty map space, choose Add waypoint, enter a name, and save (or press Enter). Check the location at multiple zoom levels. Cancel and Escape must create nothing.
- Left-click the new marker to track it. Right-click it and choose Delete. Confirm it disappears immediately and stays gone after reconnecting.
- A shared marker offers Hide on my map. Confirm it disappears only for that player, stays hidden after reconnecting, and remains visible to other network members.
- Join a shared network with substantial exploration history. Verify progressive terrain transfer, shared custom waypoints, and waypoint actions while terrain is still arriving.

## Implementation limits

- Minimap: at most 256 sample requests in a client-tick job, with new or unavailable columns prioritized and a rolling refresh cursor. HUD drawing reads prepared colors and performs no world sampling. Three rotating textures avoid updating the immediately preceding frame's terrain texture.
- Exploration: at most 64 column visits per job, nearest terrain first. An idle completed scan waits before retrying. Full radius-32 coverage is retained.
- Both paths share one job per displayed frame, including all catch-up client ticks, and at most 256 uncached samples in a 0.5 ms sampling window. A single block's modded color lookup can exceed that window; the limit stops subsequent samples. Refresh/save scheduling uses elapsed ticks so a skipped catch-up tick cannot skip a save indefinitely.
- Nearby entity lookup is cached for five client ticks; entity positions are read when rendered.
- Disk serialization and writes use snapshots on a background worker. Building a snapshot still copies local map data on the client thread.

## Diagnosis from the assisted run

Non-suspending debugger logpoints captured right-click events with `button=1`, `hovered=null`, and `context=null`. The old handler had no empty-space action. It also refused deletion of all markers present in shared navigation. The replacement computes hits at click time and supports personal creation/deletion and per-player hiding of shared markers.

A terrain logpoint captured both `incomplete=true` and `deferred=true`. The prior retry mechanism could request another whole-grid pass. The replacement uses a fixed-size cursor; unavailable samples cannot restart the entire terrain refresh.

A 60-second Java Flight Recorder capture included considerable chunk-generation work across server and worker threads. Navigation also appeared in render-thread samples. This is evidence of multiple workloads, not a measured FPS improvement or proof that navigation caused server lag. The reported symptom was low FPS/choppy camera movement; compare client frame times in the same generated area before attributing remaining stutter to navigation.

The second assisted run confirmed creation and deletion of a custom waypoint named `place` at X=-33, Z=-31. The user reported that waypoint controls worked, but that camera FPS was worse.

Inspection of the second JFR capture found navigation cache lookups in `HashMap$TreeNode.find`, `Long.equals`, and comparable-class discovery. Packing coordinates into a boxed Long makes `Long.hashCode` approximately `x ^ z`, producing many collisions for nearby coordinates. The cache now uses fastutil's `Long2ObjectOpenHashMap`, already supplied by Minecraft. An ad-hoc local benchmark checked identical values for 6,400 signed coordinate keys and measured 640,000 lookups at 86.29 ms for the boxed map versus 3.47 ms for the primitive map (best of five after warmup). This verifies the lookup improvement for that workload; it is not an FPS benchmark. In-game frame times still require the same-location enabled/disabled comparison.


The later installed/uninstalled comparison reported installed FPS of 2 moving / 17 still and uninstalled FPS of 5 moving / 21 still. A further direct JFR recording with navigation installed captured 14 navigation-related render-thread samples out of 1,371 Java/native render-thread samples; 12 were under HUD terrain updates. This supports removing world reads from HUD drawing, but does not prove that those CPU samples explain the full FPS difference. The next change shares work across catch-up ticks per displayed frame, reduces the sampling window, prioritizes nearby exploration, and rotates the GPU upload textures. An F1 HUD-hidden comparison and an in-game retest are needed before claiming an FPS improvement.

The HUD-hidden comparison was reported as about the same. This weakens the claim that drawing the minimap is the main cause of the very low overall FPS. The revised scheduler still records exploration with the HUD hidden, but skips minimap preparation when hidden, in third person, or behind another screen. Nearby exploration has priority over minimap sampling. Actual FPS improvement for this revision remains unverified; use the new jar in a fresh client process.
