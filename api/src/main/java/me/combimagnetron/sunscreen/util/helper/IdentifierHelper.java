package me.combimagnetron.sunscreen.util.helper;

import me.combimagnetron.passport.util.data.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

public class IdentifierHelper {
    private static final Map<String, Type[]> BLOCKED_NAMES_TO_TYPE = Map.of(
            "theme", new Type[]{Type.PAGE, Type.ELEMENT},
            "page", new Type[]{Type.THEME, Type.ELEMENT},
            "element", new Type[]{Type.PAGE, Type.THEME}
    );

    private IdentifierHelper() {}

    public static boolean conforms(@NotNull Identifier identifier, @NotNull Type type) {
        final String identifierString = identifier.string();
        return !(BLOCKED_NAMES_TO_TYPE.entrySet().stream()
                .filter(stringEntry -> identifierString.contains(stringEntry.getKey()))
                .map(Map.Entry::getValue)
                .filter(types -> Arrays.stream(types)
                        .anyMatch(type1 -> type1 == type)).count() > 1);
    }

    public enum Type {
        THEME, PAGE, ELEMENT,
    }

}
