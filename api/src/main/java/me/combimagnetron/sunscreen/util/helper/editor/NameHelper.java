package me.combimagnetron.sunscreen.util.helper.editor;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NameHelper {

    private NameHelper() {}

    public static @NotNull String suggestIdentifier(String displayName, String namespace) {
        String replaced = displayName.toLowerCase().trim().replaceAll(" ", "_");
        return namespace.toLowerCase() + ":menu/" + replaced;
    }

    public static @NotNull String suggestIdentifier(@NotNull String displayName) {
        return suggestIdentifier(displayName, "custom");
    }

    public static @NotNull String suggestDisplayName(@NotNull String identifier) {
        String key = identifier.split(":")[0];
        if (key == null) return "Invalid identifier";
        if (key.contains("/")) {
            String[] parts = key.split("/");
            key = parts[parts.length - 1];
        }
        String[] words = key.split("_");
        return Arrays.stream(words).map(NameHelper::capitalize).collect(Collectors.joining(" "));
    }

    private static @NotNull String capitalize(@NotNull String input) {
        final char[] buffer = input.toCharArray();
        char first = buffer[0];
        char capitalized = Character.toTitleCase(first);
        if (first == capitalized) return  input;
        return capitalized + input.substring(1);
    }

}
