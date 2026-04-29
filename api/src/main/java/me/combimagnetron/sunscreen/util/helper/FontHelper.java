package me.combimagnetron.sunscreen.util.helper;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class FontHelper {
    private static final Key FONT_KEY = Key.key("sunscreen:offset");
    private static final Map<Integer, Character> OFFSET_MAP;
    static {
        OFFSET_MAP = new HashMap<>();
        OFFSET_MAP.put(-1, 'a');
        OFFSET_MAP.put(-2, 'b');
        OFFSET_MAP.put(-4, 'c');
        OFFSET_MAP.put(-8, 'd');
        OFFSET_MAP.put(-16, 'e');
        OFFSET_MAP.put(-32, 'f');
        OFFSET_MAP.put(-64, 'g');
        OFFSET_MAP.put(-128, 'h');
        OFFSET_MAP.put(1, 'i');
        OFFSET_MAP.put(2, 'j');
        OFFSET_MAP.put(4, 'k');
        OFFSET_MAP.put(8, 'l');
        OFFSET_MAP.put(16, 'm');
        OFFSET_MAP.put(32, 'n');
        OFFSET_MAP.put(64, 'o');
        OFFSET_MAP.put(128, 'p');
    }

    private FontHelper() {}

    public static @NotNull Component offset(int pixels) {
        final StringBuilder builder = new StringBuilder();
        if (pixels == 0)
            return Component.text(builder.toString()).font(FONT_KEY);

        final boolean negative = Integer.signum(pixels) == -1;
        pixels = Math.abs(pixels);

        while (pixels > 0) {
            int highestBit = Integer.highestOneBit(pixels);
            if (highestBit > 128)
                highestBit = 128;
            builder.append(OFFSET_MAP.get(negative ? -highestBit : highestBit));

            pixels -= highestBit;
        }

        return Component.text(builder.toString()).font(FONT_KEY);
    }

}
