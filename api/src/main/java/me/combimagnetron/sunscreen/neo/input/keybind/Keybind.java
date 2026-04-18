package me.combimagnetron.sunscreen.neo.input.keybind;

import me.combimagnetron.passport.event.EventBus;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.event.UserPressKeybindEvent;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.GraphicLike;
import me.combimagnetron.sunscreen.neo.input.Interactable;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.loader.MenuComponent;
import me.combimagnetron.sunscreen.neo.loader.ComponentLoader;
import me.combimagnetron.sunscreen.neo.loader.MenuComponentLoaderContext;
import me.combimagnetron.sunscreen.neo.property.Property;
import me.combimagnetron.sunscreen.neo.property.Size;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Keybind implements ModernElement<Keybind, Canvas>, Interactable<Keybind, Keybind.KeybindListenerReferences>, MenuComponent<Keybind> {
    private final KeybindListenerReferences references = new KeybindListenerReferences(this);
    private final Registered registered;
    private final Collection<Modifier> modifiers;

    public Keybind(Registered registered, Collection<Modifier> modifiers) {
        this.registered = registered;
        this.modifiers = modifiers;
    }

    public static Keybind of(@NotNull Keybind.NamedKey registered, @NotNull Modifier... modifiers) {
        return of(registered.registered, modifiers);
    }

    public static Keybind of(@NotNull Registered registered, @NotNull Collection<Modifier> modifiers) {
        return new Keybind(registered, modifiers);
    }

    public static Keybind of(@NotNull Registered registered, @NotNull Modifier... modifiers) {
        return of(registered, Arrays.stream(modifiers).collect(Collectors.toSet()));
    }

    public static Keybind of(@NotNull Keybind.NamedKey registered, @NotNull NamedModifier... modifiers) {
        return of(registered.registered,
                Arrays.stream(modifiers).map(namedModifier -> namedModifier.modifier).collect(Collectors.toSet()));
    }

    public static Keybind of(@NotNull Registered registered, @NotNull NamedModifier... modifiers) {
        return of(registered,
                Arrays.stream(modifiers).map(namedModifier -> namedModifier.modifier).collect(Collectors.toSet()));
    }

    @Override
    public @NotNull KeybindListenerReferences listen() {
        return references;
    }

    @Override
    public <C extends InputContext<?>> @NotNull C input(Class<C> clazz) {
        return null;
    }

    public Registered registered() {
        return registered;
    }

    public Collection<Modifier> modifiers() {
        return modifiers;
    }

    @Override
    public @NotNull ComponentLoader<Keybind, MenuComponentLoaderContext> loader() {
        return null;
    }

    @Override
    public @NotNull Class<Keybind> type() {
        return Keybind.class;
    }

    @Override
    public @Nullable <T, C, P extends Property<T, C>> P property(@NotNull Class<P> propertyClass) {
        return null;
    }

    @Override
    public @NonNull <T, C> Keybind property(@NotNull Property<T, C> property) {
        return null;
    }

    @Override
    public @NotNull Collection<Property<?, ?>> properties() {
        return List.of();
    }

    @Override
    public @NotNull Canvas render(@NonNull Size property, @org.jetbrains.annotations.Nullable RenderContext context) {
        return null;
    }

    @Override
    public @NotNull Identifier identifier() {
        return null;
    }

    /**
     * Enum class containing all detectable modifier keys in Minecraft.
     */
    public enum Modifier {
        SPRINT, SNEAK, JUMP
    }

    /**
     * Enum class to wrap modifiers to default Minecraft key.
     *
     * @apiNote Do not expect these modifiers to be the actual modifiers mentioned,
     *          in case of mentioning it in label please use
     *          {@link KeybindTextMapping}.
     */
    public enum NamedModifier {
        CTRL(Modifier.SPRINT), SHIFT(Modifier.SNEAK), SPACE(Modifier.JUMP);

        private final Modifier modifier;

        NamedModifier(Modifier modifier) {
            this.modifier = modifier;
        }

    }

    /**
     * Enum class containing all detectable keys in Minecraft.
     */
    public enum Registered {
        FORWARD, BACKWARD, LEFT, RIGHT, DROP_ITEM, ADVANCEMENTS, SWAP_HAND, SLOT_1, SLOT_2, SLOT_3, SLOT_4, SLOT_5,
        SLOT_6, SLOT_7, SLOT_8, SLOT_9, OPEN_INVENTORY
    }

    /**
     * Enum class to wrap keys to default Minecraft key.
     *
     * @apiNote Do not expect these keys to be the actual key mentioned, in case of
     *          mentioning it in label please use {@link KeybindTextMapping}.
     */
    public enum NamedKey {
        W(Registered.FORWARD), S(Registered.BACKWARD), A(Registered.LEFT), D(Registered.RIGHT), Q(Registered.DROP_ITEM),
        L(Registered.ADVANCEMENTS), F(Registered.SWAP_HAND), KEY_1(Registered.SLOT_1), KEY_2(Registered.SLOT_2),
        KEY_3(Registered.SLOT_3), KEY_4(Registered.SLOT_4), KEY_5(Registered.SLOT_5), KEY_6(Registered.SLOT_6),
        KEY_7(Registered.SLOT_7), KEY_8(Registered.SLOT_8), KEY_9(Registered.SLOT_9), E(Registered.OPEN_INVENTORY);

        private final Registered registered;

        NamedKey(Registered registered) {
            this.registered = registered;
        }

    }

    public static final class KeybindListenerReferences extends ListenerReferences<Keybind, KeybindListenerReferences> {
        private final @NotNull Keybind back;
        
        public KeybindListenerReferences(@NotNull Keybind back) {
            this.back = back;
        }

        public @NotNull KeybindListenerReferences pressed(@NotNull Consumer<UserPressKeybindEvent> event) {
            EventBus.subscribe(UserPressKeybindEvent.class, event);
            return this;
        }

        @Override
        public @NotNull Keybind back() {
            return back;
        }

    }

}
