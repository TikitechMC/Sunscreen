package me.combimagnetron.sunscreen.neo.render.engine.encode;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.render.engine.binary.BinaryMasks;
import me.combimagnetron.sunscreen.neo.render.engine.binary.MapOutputStream;
import me.combimagnetron.sunscreen.neo.render.engine.exception.FatalEncodeException;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MapEncoderMoreTest {
    private final static int MAGIC_ID = 0x53554E53;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(16384);
    private final Object lock = new Object();
    private final AtomicInteger paletteId = new AtomicInteger(0);
    private final ThreadLocal<LocalPalette[]> localPalettes = ThreadLocal.withInitial(() -> new LocalPalette[47]);
    private final ThreadLocal<ImageTile[]> imageTiles = ThreadLocal.withInitial(() -> new ImageTile[4096]);
    private final ThreadLocal<BitTile[]> bitTiles = ThreadLocal.withInitial(() -> new BitTile[4096]);
    private final ThreadLocal<Map<Integer, LocalPalette>> colorToPalette = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<TreeMap<Integer, ArrayDeque<LocalPalette>>> palettesByFreeSlots = ThreadLocal.withInitial(TreeMap::new);
    private final ThreadLocal<IntOpenHashSet> scratchSet = ThreadLocal.withInitial(IntOpenHashSet::new);
    private final ThreadLocal<IntOpenHashSet> globalUniqueColors = ThreadLocal.withInitial(IntOpenHashSet::new);
    private final ThreadLocal<Map<IntArrayList, BitTile>> tileCache = ThreadLocal.withInitial(HashMap::new);
    private final BufferedColorSpace colorSpace;
    private final ProcessedRenderChunk renderChunk;

    public MapEncoderMoreTest(@NotNull ProcessedRenderChunk renderChunk) {
        this.colorSpace = renderChunk.bufferedColorSpace();
        this.renderChunk = renderChunk;

        formTiles();
        packPalettesAndTiles();
        write();
    }

    private void formTiles() {
        int index = 0;
        IntOpenHashSet global = globalUniqueColors.get();
        for (int ty = 0; ty < 64; ty++) {
            for (int tx = 0; tx < 64; tx++) {
                IntArrayList colors = IntArrayList.of(
                        colorSpace.at(tx * 2, ty * 2),
                        colorSpace.at(tx * 2 + 1, ty * 2),
                        colorSpace.at(tx * 2, ty * 2 + 1),
                        colorSpace.at(tx * 2 + 1, ty * 2 + 1)
                );
                global.addAll(colors);
                imageTiles.get()[index++] = new ImageTile(colors);
            }
        }
    }

    private void packPalettesAndTiles() {
        ImageTile[] tiles = imageTiles.get();
        if (globalUniqueColors.get().size() > 752) {
            throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
        }

        LocalPalette[] palettes = localPalettes.get();
        TreeMap<Integer, ArrayDeque<LocalPalette>> byFreeSlots = palettesByFreeSlots.get();
        for (int i = 0; i < 47; i++) {
            palettes[i] = new LocalPalette();
            byFreeSlots.computeIfAbsent(16, k -> new ArrayDeque<>()).add(palettes[i]);
        }

        Map<Integer, LocalPalette> c2p = colorToPalette.get();
        Map<IntArrayList, BitTile> cache = tileCache.get();
        IntOpenHashSet scratch = scratchSet.get();

        for (int i = 0; i < tiles.length; i++) {
            ImageTile imageTile = tiles[i];

            BitTile cached = cache.get(imageTile.colors());
            if (cached != null) {
                bitTiles.get()[i] = cached;
                continue;
            }

            LocalPalette match = c2p.get(imageTile.colors().getInt(0));
            IntArrayList indices = null;

            if (match != null) {
                indices = tryBuildIndices(imageTile, match, c2p);
            }

            if (indices == null) {
                scratch.clear();
                for (int color : imageTile.colors()) {
                    if (!c2p.containsKey(color)) scratch.add(color);
                }

                Map.Entry<Integer, ArrayDeque<LocalPalette>> entry = byFreeSlots.ceilingEntry(scratch.size());
                if (entry == null) {
                    throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
                }

                ArrayDeque<LocalPalette> candidates = entry.getValue();
                match = candidates.peek();

                int oldFree = 16 - match.colors().size();
                candidates.poll();
                if (candidates.isEmpty()) byFreeSlots.remove(oldFree);

                for (int color : scratch) {
                    match.colors().add(color);
                    c2p.put(color, match);
                }

                int newFree = 16 - match.colors().size();
                if (newFree > 0) {
                    byFreeSlots.computeIfAbsent(newFree, k -> new ArrayDeque<>()).add(match);
                }

                indices = new IntArrayList(4);
                for (int color : imageTile.colors()) {
                    indices.add(match.colors().indexOf(color));
                }
            }

            BitTile bitTile = new BitTile(indices, match);
            bitTiles.get()[i] = bitTile;
            cache.put(imageTile.colors(), bitTile);
        }

        for (LocalPalette localPalette : localPalettes.get()) {
            int missing = 16 - localPalette.colors().size();
            for (int i = 0; i < missing; i++) {
                localPalette.colors().add(6);
            }
        }
    }

    private IntArrayList tryBuildIndices(ImageTile imageTile, LocalPalette palette, Map<Integer, LocalPalette> c2p) {
        IntArrayList indices = new IntArrayList(4);
        for (int color : imageTile.colors()) {
            LocalPalette owner = c2p.get(color);
            if (owner != palette) return null;
            indices.add(palette.colors().indexOf(color));
        }
        return indices;
    }

    public ByteArrayOutputStream bytes() {
        return buffer;
    }

    private void write() {
        synchronized (lock) {
            try (MapOutputStream out = MapOutputStream.create(buffer)) {
                out.writeBits(32, MAGIC_ID);
                for (LocalPalette localPalette : localPalettes.get()) {
                    for (int color : localPalette.colors) {
                        out.writeBits(8, (color) & 0xFF);
                        out.writeBits(8, (color >>> 8) & 0xFF);
                        out.writeBits(8, (color >>> 16) & 0xFF);
                        out.writeBits(8, (color >>> 24) & 0xFF);
                    }
                }
                for (BitTile bitTile : bitTiles.get()) {
                    out.writeBits(6, bitTile.palette().serializedId() & BinaryMasks.SIX_BIT_MASK);
                    for (int index : bitTile.indices) {
                        out.writeBits(4, index & BinaryMasks.FOUR_BIT_MASK);
                    }
                }
                out.writeBits(32, Float.floatToIntBits(renderChunk.scale().floatValue()));
                out.writeBits(32, Float.floatToIntBits(renderChunk.position().x()));
                out.writeBits(32, Float.floatToIntBits(renderChunk.position().y()));
                out.writeBits(32, Float.floatToIntBits(renderChunk.position().z()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    record ImageTile(IntArrayList colors) {}

    record BitTile(IntArrayList indices, LocalPalette palette) {}

    public final class LocalPalette {
        private final IntArrayList colors = IntArrayList.of();
        private final int id;

        public LocalPalette(int id) {
            this.id = id;
        }

        public LocalPalette() {
            this(paletteId.getAndIncrement());
        }

        public byte serializedId() {
            return (byte) (id & BinaryMasks.SIX_BIT_MASK);
        }

        public IntArrayList colors() {
            return colors;
        }

        public int id() {
            return id;
        }
    }
}