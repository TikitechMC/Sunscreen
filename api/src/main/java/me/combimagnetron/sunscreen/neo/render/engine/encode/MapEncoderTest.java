package me.combimagnetron.sunscreen.neo.render.engine.encode;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.render.engine.binary.BinaryMasks;
import me.combimagnetron.sunscreen.neo.render.engine.binary.MapOutputStream;
import me.combimagnetron.sunscreen.neo.render.engine.exception.FatalEncodeException;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MapEncoderTest {
    private final static int MAGIC_ID = 0x53554E53;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(16384);
    private final Object lock = new Object();
    private final AtomicInteger paletteId = new AtomicInteger(0);
    private final ThreadLocal<LocalPalette[]> localPalettes = ThreadLocal.withInitial(() -> new LocalPalette[47]);
    private final ThreadLocal<ImageTile[]> imageTiles = ThreadLocal.withInitial(() -> new ImageTile[4096]);
    private final ThreadLocal<BitTile[]> bitTiles = ThreadLocal.withInitial(() -> new BitTile[4096]);
    private final BufferedColorSpace colorSpace;
    private final ProcessedRenderChunk renderChunk;

    public MapEncoderTest(@NotNull ProcessedRenderChunk renderChunk) {
        this.colorSpace = renderChunk.bufferedColorSpace();
        this.renderChunk = renderChunk;

        formTiles();
        packPalettesAndTiles();
        write();
    }

    private void formTiles() {
        ImageTile[] tiles = imageTiles.get();
        int index = 0;
        for (int ty = 0; ty < 64; ty++) {
            int y2 = ty * 2;
            for (int tx = 0; tx < 64; tx++) {
                int x2 = tx * 2;
                // Pre-allocate with exact size and reuse array
                int[] colors = new int[4];
                colors[0] = colorSpace.at(x2, y2);
                colors[1] = colorSpace.at(x2 + 1, y2);
                colors[2] = colorSpace.at(x2, y2 + 1);
                colors[3] = colorSpace.at(x2 + 1, y2 + 1);
                tiles[index++] = new ImageTile(colors);
            }
        }
    }

    private static int allUniqueColorAmount(ImageTile[] imageTiles) {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet(752); // Pre-size
        for (ImageTile imageTile : imageTiles) {
            for (int color : imageTile.colors) {
                intOpenHashSet.add(color);
            }
        }
        return intOpenHashSet.size();
    }

    private void packPalettesAndTiles() {
        ImageTile[] tiles = imageTiles.get();
        if (allUniqueColorAmount(tiles) > 752) {
            throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
        }

        LocalPalette[] palettes = localPalettes.get();
        for (int i = 0; i < 47; i++) {
            palettes[i] = new LocalPalette();
        }

        BitTile[] bitTilesArray = bitTiles.get();

        for (int i = 0; i < tiles.length; i++) {
            ImageTile imageTile = tiles[i];
            IntArrayList indices = null;
            LocalPalette match = null;

            // Check each palette for a match
            for (LocalPalette palette : palettes) {
                // Fast path: Check if palette already contains all colors
                indices = palette.tryGetIndices(imageTile.colors);
                if (indices != null) {
                    match = palette;
                    break;
                }

                // Slow path: Try to add missing colors if there's space
                IntOpenHashSet uniqueColors = new IntOpenHashSet(4);
                for (int color : imageTile.colors) {
                    if (!palette.containsColor(color)) {
                        uniqueColors.add(color);
                    }
                }

                if (palette.size() + uniqueColors.size() <= 16) {
                    // Add new colors to palette
                    for (int color : uniqueColors) {
                        palette.addColor(color);
                    }

                    // Get indices for all tile colors
                    indices = new IntArrayList(4);
                    for (int color : imageTile.colors) {
                        indices.add(palette.getIndex(color));
                    }
                    match = palette;
                    break;
                }
            }

            if (indices == null || indices.isEmpty()) {
                throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
            }

            bitTilesArray[i] = new BitTile(indices, match);
        }

        // Pad palettes to 16 colors
        for (LocalPalette palette : palettes) {
            palette.padTo16();
        }
    }

    public ByteArrayOutputStream bytes() {
        return buffer;
    }

    private void write() {
        try (MapOutputStream out = MapOutputStream.create(buffer)) {
            out.writeBits(32, MAGIC_ID);
            // Write palettes
            for (LocalPalette palette : localPalettes.get()) {
                for (int color : palette.colors) {
                    out.writeBits(8, color & 0xFF);
                    out.writeBits(8, (color >>> 8) & 0xFF);
                    out.writeBits(8, (color >>> 16) & 0xFF);
                    out.writeBits(8, (color >>> 24) & 0xFF);
                }
            }
            // Write tiles
            for (BitTile bitTile : bitTiles.get()) {
                out.writeBits(6, bitTile.palette.serializedId() & BinaryMasks.SIX_BIT_MASK);
                for (int index : bitTile.indices) {
                    out.writeBits(4, index & BinaryMasks.FOUR_BIT_MASK);
                }
            }
            // Write metadata
            out.writeBits(32, Float.floatToIntBits(renderChunk.scale().floatValue()));
            out.writeBits(32, Float.floatToIntBits(renderChunk.position().x()));
            out.writeBits(32, Float.floatToIntBits(renderChunk.position().y()));
            out.writeBits(32, Float.floatToIntBits(renderChunk.position().z()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    record ImageTile(int[] colors) {}

    record BitTile(IntArrayList indices, LocalPalette palette) {}

    public final class LocalPalette {
        private final IntArrayList colors = new IntArrayList(16);
        private final Int2IntOpenHashMap colorToIndex = new Int2IntOpenHashMap(16);
        private final int id;

        public LocalPalette(int id) {
            this.id = id;
            this.colorToIndex.defaultReturnValue(-1);
        }

        public LocalPalette() {
            this(paletteId.getAndIncrement());
        }

        public boolean addColor(int color) {
            if (colorToIndex.containsKey(color)) {
                return false;
            }
            int index = colors.size();
            colors.add(color);
            colorToIndex.put(color, index);
            return true;
        }

        public boolean containsColor(int color) {
            return colorToIndex.containsKey(color);
        }

        public int getIndex(int color) {
            return colorToIndex.get(color);
        }

        public int size() {
            return colors.size();
        }

        public IntArrayList tryGetIndices(int[] tileColors) {
            IntArrayList indices = new IntArrayList(tileColors.length);
            for (int color : tileColors) {
                int idx = colorToIndex.get(color);
                if (idx == -1) {
                    return null;
                }
                indices.add(idx);
            }
            return indices;
        }

        public void padTo16() {
            int missing = 16 - colors.size();
            for (int i = 0; i < missing; i++) {
                colors.add(6);
            }
            colorToIndex.putIfAbsent(6, colors.indexOf(6));
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