package me.combimagnetron.sunscreen.neo.editor.input;

import com.google.common.collect.ImmutableMap;
import me.combimagnetron.passport.util.data.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SelectorInputContext(@NotNull ImmutableMap<Identifier, InnerContext<?>> innerContexts) {

    public interface InnerContext<T> {

        @NotNull T @NotNull [] values();

        int size();

        default @Nullable T index(int index) {
            if (index > size() - 1) return null;
            return values()[index];
        }

    }

    public record MultiValueInnerContext(Integer[] values) implements InnerContext<Integer> {
        private final static int SIZE = 8;

        @Override
        public int size() {
            return SIZE;
        }

    }


    public record ValueInnerContext(Integer[] values) implements InnerContext<Integer> {
        private final static int SIZE = 4;

        @Override
        public int size() {
            return SIZE;
        }

    }

}
