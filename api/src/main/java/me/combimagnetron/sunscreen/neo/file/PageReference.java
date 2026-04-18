package me.combimagnetron.sunscreen.neo.file;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.page.Page;
import org.jetbrains.annotations.NotNull;

public record PageReference(@NotNull Identifier id, @NotNull String file, boolean isRoot) {

    public static @NotNull PageReference root(@NotNull Identifier id, @NotNull String file) {
        return new PageReference(id, file, true);
    }

    public static @NotNull PageReference page(@NotNull Identifier id, @NotNull String file) {
        return new PageReference(id, file, false);
    }

}
