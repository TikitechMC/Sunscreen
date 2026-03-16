package me.combimagnetron.sunscreen.neo.render.engine.encode;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.render.engine.binary.BinaryMasks;
import me.combimagnetron.sunscreen.neo.render.engine.binary.MapOutputStream;
import me.combimagnetron.sunscreen.neo.render.engine.exception.FatalEncodeException;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class MapEncoderIHateMyselfMore {
    private static final int MAGIC_ID = 0x53554E53;
    private static final int TILE_COUNT = 4096;
    private static final int PALETTE_COUNT = 47;
    private static final int MAX_COLORS = 752;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(16384);
    private final AtomicInteger paletteId = new AtomicInteger(0);
    private final LocalPalette[] localPalettes = new LocalPalette[PALETTE_COUNT];
    private final int[] tilePaletteIndices = new int[16384];
    private final int[] tilePaletteIds = new int[TILE_COUNT];
    private final int[] scratchColors = new int[4];
    private final Int2IntOpenHashMap globalColorLookup = new Int2IntOpenHashMap(MAX_COLORS);
    private final BufferedColorSpace colorSpace;
    private final ProcessedRenderChunk renderChunk;

    public MapEncoderIHateMyselfMore(@NotNull ProcessedRenderChunk renderChunk) {
        this.colorSpace = renderChunk.bufferedColorSpace();
        this.renderChunk = renderChunk;
        this.globalColorLookup.defaultReturnValue(-1);

        packPalettesAndTiles();
        write();
    }

    private void packPalettesAndTiles() {
        for (int i = 0; i < PALETTE_COUNT; i++) {
            localPalettes[i] = new LocalPalette();
        }

        for (int i = 0; i < TILE_COUNT; i++) {
            int ty = i / 64, tx = i % 64;
            int x2 = tx * 2, y2 = ty * 2;
            scratchColors[0] = colorSpace.at(x2, y2);
            scratchColors[1] = colorSpace.at(x2 + 1, y2);
            scratchColors[2] = colorSpace.at(x2,y2 + 1);
            scratchColors[3] = colorSpace.at(x2 + 1, y2 + 1);

            LocalPalette matched = null;

            boolean allCached = true;
            for (int j = 0; j < 4; j++) {
                int packed = globalColorLookup.get(scratchColors[j]);
                if (packed == -1) { allCached = false; break; }
                tilePaletteIndices[i * 4 + j] = packed & 0xF;
                if (matched == null) matched = localPalettes[packed >>> 4];
                else if (matched != localPalettes[packed >>> 4]) { allCached = false; break; }
            }

            if (allCached && matched != null) {
                tilePaletteIds[i] = matched.id();
                continue;
            }

            matched = null;
            for (LocalPalette palette : localPalettes) {
                int missing = 0;
                for (int color : scratchColors) {
                    if (!palette.containsColor(color)) missing++;
                }
                if (palette.size() + missing <= 16) {
                    matched = palette;
                    break;
                }
            }

            if (matched == null) {
                throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
            }

            for (int color : scratchColors) {
                if (!matched.containsColor(color)) {
                    if (globalColorLookup.size() >= MAX_COLORS) {
                        throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
                    }
                    matched.addColor(color);
                    globalColorLookup.put(color, (matched.id() << 4) | matched.getIndex(color));
                }
            }

            for (int j = 0; j < 4; j++) {
                tilePaletteIndices[i * 4 + j] = matched.getIndex(scratchColors[j]);
            }
            tilePaletteIds[i] = matched.id();
        }

        for (LocalPalette palette : localPalettes) {
            palette.padTo16();
        }
    }

    public ByteArrayOutputStream bytes() {
        return buffer;
    }

    private void write() {
        try (MapOutputStream out = MapOutputStream.create(buffer)) {
            out.writeBits(32, MAGIC_ID);
            for (LocalPalette palette : localPalettes) {
                for (int color : palette.colors()) {
                    out.writeBits(8, color & 0xFF);
                    out.writeBits(8, (color >>> 8) & 0xFF);
                    out.writeBits(8, (color >>> 16) & 0xFF);
                    out.writeBits(8, (color >>> 24) & 0xFF);
                }
            }
            for (int i = 0; i < TILE_COUNT; i++) {
                int base = i * 4;
                out.writeBits(6, tilePaletteIds[i] & BinaryMasks.SIX_BIT_MASK);
                out.writeBits(4, tilePaletteIndices[base] & BinaryMasks.FOUR_BIT_MASK);
                out.writeBits(4, tilePaletteIndices[base + 1] & BinaryMasks.FOUR_BIT_MASK);
                out.writeBits(4, tilePaletteIndices[base + 2] & BinaryMasks.FOUR_BIT_MASK);
                out.writeBits(4, tilePaletteIndices[base + 3] & BinaryMasks.FOUR_BIT_MASK);
            }
            out.writeBits(32, Float.floatToIntBits(renderChunk.scale().floatValue()));
            out.writeBits(32, Float.floatToIntBits(renderChunk.position().x()));
            out.writeBits(32, Float.floatToIntBits(renderChunk.position().y()));
            out.writeBits(32, Float.floatToIntBits(renderChunk.position().z()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
            if (colorToIndex.containsKey(color)) return false;
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