package me.combimagnetron.sunscreen.neo.render.engine.encode;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.combimagnetron.sunscreen.neo.graphic.BufferedColorSpace;
import me.combimagnetron.sunscreen.neo.render.engine.exception.FatalEncodeException;
import me.combimagnetron.sunscreen.neo.render.engine.grid.ProcessedRenderChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MapEncoderIHateMyselfMore {
    private static final int MAGIC_ID = 0x53554E53;
    private static final int TILE_COUNT = 4096;
    private static final int PALETTE_COUNT = 47;
    private static final int MAX_COLORS = 752;
    private static final int WIDTH = 128;
    private static final int OFFSET = 4;
    private static final int TARGET_SIZE = 16384;
    private static final int RAW_BYTES = 14292;

    private static final int[] GATHER_INDICES = buildGatherIndices();

    private final LocalPalette[] localPalettes = new LocalPalette[PALETTE_COUNT];
    private final int[] tileIdx0 = new int[TILE_COUNT];
    private final int[] tileIdx1 = new int[TILE_COUNT];
    private final int[] tileIdx2 = new int[TILE_COUNT];
    private final int[] tileIdx3 = new int[TILE_COUNT];
    private final int[] tilePaletteIds = new int[TILE_COUNT];
    private final Int2IntOpenHashMap globalColorLookup = new Int2IntOpenHashMap(MAX_COLORS);
    private final int[] tilePixels = new int[TILE_COUNT * 4];
    private final int[] packedTiles = new int[TILE_COUNT];
    private final byte[] raw = new byte[RAW_BYTES + 8];
    private final byte[] encoded = new byte[TARGET_SIZE];
    private int paletteId;

    public MapEncoderIHateMyselfMore() {
        for (int i = 0; i < PALETTE_COUNT; i++) {
            localPalettes[i] = new LocalPalette(i);
        }
    }

    private static int[] buildGatherIndices() {
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
        return indices;
    }

    public byte[] encode(@NotNull ProcessedRenderChunk renderChunk) {
        reset();
        gatherPixels(renderChunk.bufferedColorSpace());
        packPalettesAndTiles();
        return write(renderChunk);
    }

    private void reset() {
        paletteId = 0;
        globalColorLookup.clear();
        for (LocalPalette palette : localPalettes) {
            palette.reset(paletteId++);
        }
    }

    private void gatherPixels(BufferedColorSpace colorSpace) {
        int[] rawBuffer = colorSpace.buffer();
        for (int i = 0; i < TILE_COUNT * 4; i++) {
            tilePixels[i] = rawBuffer[GATHER_INDICES[i]];
        }
    }

    private void packPalettesAndTiles() {
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
                    tileIdx0[i] = p0 & 0xF;
                    tileIdx1[i] = p1 & 0xF;
                    tileIdx2[i] = p2 & 0xF;
                    tileIdx3[i] = p3 & 0xF;
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
                if (palette.size + missing <= 16) {
                    matched = palette;
                    break;
                }
            }

            if (matched == null) {
                throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
            }

            addColorIfMissing(matched, c0);
            addColorIfMissing(matched, c1);
            addColorIfMissing(matched, c2);
            addColorIfMissing(matched, c3);

            tileIdx0[i] = matched.getIndex(c0);
            tileIdx1[i] = matched.getIndex(c1);
            tileIdx2[i] = matched.getIndex(c2);
            tileIdx3[i] = matched.getIndex(c3);
            tilePaletteIds[i] = matched.id;
        }

        for (LocalPalette palette : localPalettes) {
            palette.padTo16();
        }
    }

    private void addColorIfMissing(LocalPalette palette, int color) {
        if (!palette.containsColor(color)) {
            if (globalColorLookup.size() >= MAX_COLORS) {
                throw new FatalEncodeException("We did not implement anything beyond this, make simpler art.");
            }
            palette.addColor(color);
            globalColorLookup.put(color, (palette.id << 4) | palette.getIndex(color));
        }
    }

    private byte[] write(ProcessedRenderChunk renderChunk) {
        int pos = 0;

        raw[pos++] = (byte) (MAGIC_ID >>> 24);
        raw[pos++] = (byte) (MAGIC_ID >>> 16);
        raw[pos++] = (byte) (MAGIC_ID >>> 8);
        raw[pos++] = (byte) (MAGIC_ID);

        for (LocalPalette palette : localPalettes) {
            int[] colors = palette.colors.elements();
            int size = palette.size;
            for (int c = 0; c < size; c++) {
                int color = colors[c];
                raw[pos++] = (byte) (color);
                raw[pos++] = (byte) (color >>> 8);
                raw[pos++] = (byte) (color >>> 16);
                raw[pos++] = (byte) (color >>> 24);
            }
        }

        for (int i = 0; i < TILE_COUNT; i++) {
            packedTiles[i] = ((tilePaletteIds[i] & 0x3F) << 16)
                    | ((tileIdx0[i] & 0xF) << 12)
                    | ((tileIdx1[i] & 0xF) << 8)
                    | ((tileIdx2[i] & 0xF) << 4)
                    | (tileIdx3[i] & 0xF);
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

        writeInt(raw, pos, Float.floatToIntBits(renderChunk.scale().floatValue())); pos += 4;
        writeInt(raw, pos, Float.floatToIntBits(renderChunk.position().x()));       pos += 4;
        writeInt(raw, pos, Float.floatToIntBits(renderChunk.position().y()));       pos += 4;
        writeInt(raw, pos, Float.floatToIntBits(renderChunk.position().z()));       pos += 4;

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

        return Arrays.copyOf(encoded, TARGET_SIZE);
    }

    private static void writeInt(byte[] dest, int pos, int value) {
        dest[pos]     = (byte) (value >>> 24);
        dest[pos + 1] = (byte) (value >>> 16);
        dest[pos + 2] = (byte) (value >>> 8);
        dest[pos + 3] = (byte) (value);
    }

    static final class LocalPalette {
        final IntArrayList colors = new IntArrayList(16);
        final Int2IntOpenHashMap colorToIndex = new Int2IntOpenHashMap(16);
        int id;
        int size;

        LocalPalette(int id) {
            this.id = id;
            this.colorToIndex.defaultReturnValue(-1);
        }

        void reset(int newId) {
            this.id = newId;
            this.size = 0;
            colors.clear();
            colorToIndex.clear();
        }

        void addColor(int color) {
            int index = size;
            colors.add(color);
            colorToIndex.put(color, index);
            size++;
        }

        boolean containsColor(int color) {
            return colorToIndex.containsKey(color);
        }

        int getIndex(int color) {
            return colorToIndex.get(color);
        }

        void padTo16() {
            int missing = 16 - size;
            for (int i = 0; i < missing; i++) {
                colors.add(6);
            }
            size = colors.size();
            colorToIndex.putIfAbsent(6, colors.indexOf(6));
        }
    }
}
