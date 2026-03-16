package me.combimagnetron.sunscreen.neo.render.engine.encode;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.render.engine.exception.FatalEncodeException;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class MapEncoderSimd {
    private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
    private static final int LANES = SPECIES.length();

    private static final int MAGIC_ID = 0x53554E53;
    private static final int TILE_COUNT = 4096;
    private static final int PALETTE_COUNT = 47;
    private static final int MAX_COLORS = 752;
    private static final int WIDTH = 128;
    private static final int OFFSET = 4;
    private static final int TARGET_SIZE = 16384;
    private static final int RAW_BYTES = 14292;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(TARGET_SIZE);
    private final AtomicInteger paletteId = new AtomicInteger(0);
    private final LocalPalette[] localPalettes = new LocalPalette[PALETTE_COUNT];
    private final int[] tilePaletteIndices = new int[TILE_COUNT * 4];
    private final int[] tilePaletteIds = new int[TILE_COUNT];
    private final Int2IntOpenHashMap globalColorLookup = new Int2IntOpenHashMap(MAX_COLORS);
    private final BufferedColorSpace colorSpace;
    private final ProcessedRenderChunk renderChunk;
    private final int[] tilePixels = new int[TILE_COUNT * 4];

    public MapEncoderSimd(@NotNull ProcessedRenderChunk renderChunk) {
        this.colorSpace = renderChunk.bufferedColorSpace();
        this.renderChunk = renderChunk;
        this.globalColorLookup.defaultReturnValue(-1);

        gatherPixels();
        packPalettesAndTiles();
        write();
    }

    private void gatherPixels() {
        int[] rawBuffer = colorSpace.buffer();
        int[] indices = new int[TILE_COUNT * 4];

        for (int i = 0; i < TILE_COUNT; i++) {
            int ty = i / 64, tx = i % 64;
            int x2 = tx * 2, y2 = ty * 2;
            int base = y2 * WIDTH + x2;
            indices[i * 4]     = base;
            indices[i * 4 + 1] = base + 1;
            indices[i * 4 + 2] = base + WIDTH;
            indices[i * 4 + 3] = base + WIDTH + 1;
        }

        int totalIndices = TILE_COUNT * 4;
        int i = 0;
        for (; i + LANES <= totalIndices; i += LANES) {
            IntVector.fromArray(SPECIES, rawBuffer, 0, indices, i).intoArray(tilePixels, i);
        }
        for (; i < totalIndices; i++) {
            tilePixels[i] = rawBuffer[indices[i]];
        }
    }

    private void packPalettesAndTiles() {
        for (int i = 0; i < PALETTE_COUNT; i++) {
            localPalettes[i] = new LocalPalette();
        }

        for (int i = 0; i < TILE_COUNT; i++) {
            int base = i * 4;
            int c0 = tilePixels[base];
            int c1 = tilePixels[base + 1];
            int c2 = tilePixels[base + 2];
            int c3 = tilePixels[base + 3];

            int p0 = globalColorLookup.get(c0);
            int p1 = globalColorLookup.get(c1);
            int p2 = globalColorLookup.get(c2);
            int p3 = globalColorLookup.get(c3);

            if ((p0 | p1 | p2 | p3) >= 0) {
                int palId = p0 >>> 4;
                if ((p1 >>> 4) == palId && (p2 >>> 4) == palId && (p3 >>> 4) == palId) {
                    tilePaletteIndices[base]     = p0 & 0xF;
                    tilePaletteIndices[base + 1] = p1 & 0xF;
                    tilePaletteIndices[base + 2] = p2 & 0xF;
                    tilePaletteIndices[base + 3] = p3 & 0xF;
                    tilePaletteIds[i] = palId;
                    continue;
                }
            }

            LocalPalette matched = null;
            for (LocalPalette palette : localPalettes) {
                int missing = 0;
                if (!palette.containsColor(c0)) missing++;
                if (!palette.containsColor(c1)) missing++;
                if (!palette.containsColor(c2)) missing++;
                if (!palette.containsColor(c3)) missing++;
                if (palette.size() + missing <= 16) {
                    matched = palette;
                    break;
                }
            }

            if (matched == null) {
                throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
            }

            int[] colors = {c0, c1, c2, c3};
            for (int color : colors) {
                if (!matched.containsColor(color)) {
                    if (globalColorLookup.size() >= MAX_COLORS) {
                        throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
                    }
                    matched.addColor(color);
                    globalColorLookup.put(color, (matched.id() << 4) | matched.getIndex(color));
                }
            }

            tilePaletteIndices[base]     = matched.getIndex(c0);
            tilePaletteIndices[base + 1] = matched.getIndex(c1);
            tilePaletteIndices[base + 2] = matched.getIndex(c2);
            tilePaletteIndices[base + 3] = matched.getIndex(c3);
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
        byte[] raw = new byte[RAW_BYTES + 8];
        int pos = 0;

        raw[pos++] = (byte) (MAGIC_ID >>> 24);
        raw[pos++] = (byte) (MAGIC_ID >>> 16);
        raw[pos++] = (byte) (MAGIC_ID >>> 8);
        raw[pos++] = (byte) (MAGIC_ID);

        for (LocalPalette palette : localPalettes) {
            int[] colors = palette.colors().toIntArray();
            for (int color : colors) {
                raw[pos++] = (byte) (color);
                raw[pos++] = (byte) (color >>> 8);
                raw[pos++] = (byte) (color >>> 16);
                raw[pos++] = (byte) (color >>> 24);
            }
        }

        int[] packedTiles = new int[TILE_COUNT];
        int[] idx0 = new int[TILE_COUNT];
        int[] idx1 = new int[TILE_COUNT];
        int[] idx2 = new int[TILE_COUNT];
        int[] idx3 = new int[TILE_COUNT];
        for (int i = 0; i < TILE_COUNT; i++) {
            int base = i * 4;
            idx0[i] = tilePaletteIndices[base];
            idx1[i] = tilePaletteIndices[base + 1];
            idx2[i] = tilePaletteIndices[base + 2];
            idx3[i] = tilePaletteIndices[base + 3];
        }

        int i = 0;
        for (; i + LANES <= TILE_COUNT; i += LANES) {
            IntVector palIds = IntVector.fromArray(SPECIES, tilePaletteIds, i).and(0x3F);
            IntVector v0 = IntVector.fromArray(SPECIES, idx0, i).and(0xF);
            IntVector v1 = IntVector.fromArray(SPECIES, idx1, i).and(0xF);
            IntVector v2 = IntVector.fromArray(SPECIES, idx2, i).and(0xF);
            IntVector v3 = IntVector.fromArray(SPECIES, idx3, i).and(0xF);

            palIds.lanewise(VectorOperators.LSHL, 16)
                    .or(v0.lanewise(VectorOperators.LSHL, 12))
                    .or(v1.lanewise(VectorOperators.LSHL, 8))
                    .or(v2.lanewise(VectorOperators.LSHL, 4))
                    .or(v3)
                    .intoArray(packedTiles, i);
        }
        for (; i < TILE_COUNT; i++) {
            packedTiles[i] = ((tilePaletteIds[i] & 0x3F) << 16)
                    | ((idx0[i] & 0xF) << 12)
                    | ((idx1[i] & 0xF) << 8)
                    | ((idx2[i] & 0xF) << 4)
                    | (idx3[i] & 0xF);
        }

        for (int t = 0; t < TILE_COUNT; t += 4) {
            long hi = ((long) packedTiles[t] << 42) | ((long) packedTiles[t + 1] << 20)
                    | ((long) packedTiles[t + 2] >>> 2);
            long lo = ((long) (packedTiles[t + 2] & 0x3) << 62) | ((long) packedTiles[t + 3] << 40);

            raw[pos++] = (byte) (hi >>> 56);
            raw[pos++] = (byte) (hi >>> 48);
            raw[pos++] = (byte) (hi >>> 40);
            raw[pos++] = (byte) (hi >>> 32);
            raw[pos++] = (byte) (hi >>> 24);
            raw[pos++] = (byte) (hi >>> 16);
            raw[pos++] = (byte) (hi >>> 8);
            raw[pos++] = (byte) (hi);
            raw[pos++] = (byte) (lo >>> 56);
            raw[pos++] = (byte) (lo >>> 48);
            raw[pos++] = (byte) (lo >>> 40);
        }

        writeFooterInt(raw, pos, Float.floatToIntBits(renderChunk.scale().floatValue())); pos += 4;
        writeFooterInt(raw, pos, Float.floatToIntBits(renderChunk.position().x()));       pos += 4;
        writeFooterInt(raw, pos, Float.floatToIntBits(renderChunk.position().y()));       pos += 4;
        writeFooterInt(raw, pos, Float.floatToIntBits(renderChunk.position().z()));       pos += 4;

        byte[] encoded = new byte[TARGET_SIZE];
        int rawPos = 0, encPos = 0;

        int fullBlocks = pos / 7;
        for (int b = 0; b < fullBlocks; b++) {
            long bits = ((long) (raw[rawPos] & 0xFF) << 48)
                    | ((long) (raw[rawPos + 1] & 0xFF) << 40)
                    | ((long) (raw[rawPos + 2] & 0xFF) << 32)
                    | ((long) (raw[rawPos + 3] & 0xFF) << 24)
                    | ((long) (raw[rawPos + 4] & 0xFF) << 16)
                    | ((long) (raw[rawPos + 5] & 0xFF) << 8)
                    | (raw[rawPos + 6] & 0xFF);
            rawPos += 7;

            encoded[encPos]     = (byte) (((int) (bits >>> 49) & 0x7F) + OFFSET);
            encoded[encPos + 1] = (byte) (((int) (bits >>> 42) & 0x7F) + OFFSET);
            encoded[encPos + 2] = (byte) (((int) (bits >>> 35) & 0x7F) + OFFSET);
            encoded[encPos + 3] = (byte) (((int) (bits >>> 28) & 0x7F) + OFFSET);
            encoded[encPos + 4] = (byte) (((int) (bits >>> 21) & 0x7F) + OFFSET);
            encoded[encPos + 5] = (byte) (((int) (bits >>> 14) & 0x7F) + OFFSET);
            encoded[encPos + 6] = (byte) (((int) (bits >>> 7) & 0x7F) + OFFSET);
            encoded[encPos + 7] = (byte) (((int) bits & 0x7F) + OFFSET);
            encPos += 8;
        }

        if (rawPos < pos) {
            long bits = 0;
            int remaining = pos - rawPos;
            for (int r = 0; r < remaining; r++) {
                bits = (bits << 8) | (raw[rawPos + r] & 0xFF);
            }
            int remainingBits = remaining * 8;
            while (remainingBits >= 7) {
                remainingBits -= 7;
                encoded[encPos++] = (byte) (((int) (bits >>> remainingBits) & 0x7F) + OFFSET);
            }
            if (remainingBits > 0) {
                encoded[encPos++] = (byte) ((((int) bits << (7 - remainingBits)) & 0x7F) + OFFSET);
            }
        }

        if (encPos < TARGET_SIZE) {
            Arrays.fill(encoded, encPos, TARGET_SIZE, (byte) OFFSET);
        }

        buffer.write(encoded, 0, TARGET_SIZE);
    }

    private static void writeFooterInt(byte[] dest, int pos, int value) {
        dest[pos]     = (byte) (value >>> 24);
        dest[pos + 1] = (byte) (value >>> 16);
        dest[pos + 2] = (byte) (value >>> 8);
        dest[pos + 3] = (byte) (value);
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

        public IntArrayList colors() {
            return colors;
        }

        public int id() {
            return id;
        }
    }
}
