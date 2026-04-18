package me.combimagnetron.sunscreen.neo.file;

import me.combimagnetron.passport.util.data.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public record MenuMetadata(@NotNull Identifier identifier, @NotNull String author, @NotNull String minVersion, @Nullable String maxVersion, @NotNull String originalVersion) {

}
