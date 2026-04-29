package me.combimagnetron.sunscreen.neo.layout;

import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.neo.element.ElementContainer;
import me.combimagnetron.sunscreen.neo.element.Elements;
import me.combimagnetron.sunscreen.neo.element.GenericInteractableModernElement;
import me.combimagnetron.sunscreen.neo.element.ModernElement;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.input.InputHandler;
import me.combimagnetron.sunscreen.neo.input.Interactable;
import me.combimagnetron.sunscreen.neo.input.ListenerReferences;
import me.combimagnetron.sunscreen.neo.input.context.InputContext;
import me.combimagnetron.sunscreen.neo.input.context.MouseInputContext;
import me.combimagnetron.sunscreen.neo.property.*;
import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.sunscreen.neo.render.engine.context.RenderContext;
import me.combimagnetron.sunscreen.util.helper.PropertyHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

@SuppressWarnings("unchecked")
public interface Layout<E extends ModernElement<E, Canvas>> extends ElementContainer<Layout<E>> {

     @NotNull E root();

     @Nullable ModernElement<?, Canvas> child(@NotNull Identifier identifier);

     @NotNull Layout<E> addUnchecked(ModernElement<?, Canvas> element);

     static @NotNull FlowLayout<?> flow(@NotNull Identifier identifier) {
         return new FlowLayout<>(identifier);
     }

     static @NotNull FlowLayout<?> flow(@NotNull Identifier identifier, @NotNull ModernElement<?, Canvas> elementLike) {
         return new FlowLayout<>(identifier, elementLike);
     }

     static @NotNull FlowLayout<?> flow(@NotNull Identifier identifier, @NotNull ModernElement<?, Canvas> @NotNull... elementLike) {
         return new FlowLayout<>(identifier, elementLike);
     }

     static @NotNull GroupLayout<?> group(@NotNull Identifier identifier, @NotNull ModernElement<?, Canvas> @NotNull... elementLike) {
         return new GroupLayout<>(identifier, elementLike);
     }

     void inputHandler(@NotNull InputHandler handler);

     class FlowLayout<E extends ModernElement<E, Canvas>> implements Layout<E> {
         private final PropertyMap propertyMap = new PropertyMap();
         private final List<ModernElement<?, Canvas>> elements = new ArrayList<>();
         private final Identifier identifier;
         private InputHandler handler;

         protected FlowLayout(Identifier identifier, ModernElement<?, Canvas>... elementLikes) {
             this.identifier = identifier;
             elements.addAll(List.of(elementLikes));
         }

         @Override
         public @NonNull E root() {
             return (E) elements.getFirst();
         }

         @Override
         public @Nullable ModernElement<?, Canvas> child(@NotNull Identifier identifier) {
             return elements.stream().filter(element -> element.identifier() == identifier).findAny().orElseThrow();
         }

         @Override
         public @NotNull Layout<E> addUnchecked(ModernElement<?, Canvas> element) {
             return null;
         }

         @Override
         public void inputHandler(@NotNull InputHandler handler) {
             this.handler = handler;
         }

         @Override
         public @NotNull Collection<ModernElement<?, Canvas>> children() {
             return elements;
         }

         @Override
         public @NotNull <L extends ModernElement<L, Canvas>> Layout<E> add(@NotNull L elementLike) {
             elements.add(elementLike);
             return this;
         }

         @Override
         public @NotNull <L extends ModernElement<L, Canvas>> Layout<E> add(@NotNull Iterable<@NotNull L> elementLikes) {
             elementLikes.forEach(elements::add);
             return this;
         }

         @Override
         public @NotNull <L extends ModernElement<L, Canvas>> Layout<E> remove(@NotNull L elementLike) {
             elements.remove(elementLike);
             return this;
         }

         @Override
         public @NotNull ElementContainer<@NotNull Layout<E>> remove(@NotNull Identifier identifier) {
             elements.removeIf(elementLike -> elementLike.identifier().anyMatch(identifier, Identifier.TestDepth.WHOLE));
             return this;
         }

         @Override
         public @NotNull Identifier identifier() {
             return identifier;
         }

         @Override
         public <T, C, P extends Property<T, C>> @NotNull P property(@NotNull Class<P> propertyClass) {
             return (P) propertyMap.get(propertyClass);
         }

         @Override
         public @NonNull <T, C> Layout<E> property(@NotNull Property<T, C> property) {
             propertyMap.put((Class<? extends Property<?, ?>>) property.getClass(), property);
             return this;
         }

         @Override
         public @NotNull Collection<Property<?, ?>> properties() {
             return List.of();
         }

         @Override
         public @NonNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
             Vec2i calculatedSize = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
             Canvas finalCanvas = Canvas.empty(calculatedSize);
             int rowHeight = 0;
             int currentX = 0;
             int currentY = 0;
             for (ModernElement<?, Canvas> value : elements) {
                 Vec2i sizeVec = PropertyHelper.vectorOrThrow(value.size(), Vec2i.class);
                 if (currentX + sizeVec.x() > calculatedSize.x()) {
                     currentY += rowHeight;
                     currentX = 0;
                     rowHeight = 0;
                 }
                 finalCanvas.place(value.render(property, context), Vec2i.of(currentX, currentY));
                 currentX += sizeVec.x();
                 rowHeight = Math.max(rowHeight, sizeVec.y());
             }
             return finalCanvas;
         }

     }

    class GroupLayout<E extends ModernElement<E, Canvas>> implements Layout<E> {
        private final PropertyMap propertyMap = new PropertyMap();
        protected final Map<Identifier, ModernElement<?, Canvas>> elements = new LinkedHashMap<>();
        private final Identifier identifier;
        private InputHandler handler;

        protected GroupLayout(Identifier identifier, ModernElement<?, Canvas>... elementLikes) {
            this.identifier = identifier;
            for (ModernElement<?, Canvas> elementLike : elementLikes) {
                elements.put(elementLike.identifier(), elementLike);
            }
        }

        @Override
        public @NonNull E root() {
            return (E) elements.values().stream().findFirst().orElseThrow();
        }

        @Override
        public void inputHandler(@NotNull InputHandler handler) {
            this.handler = handler;
            for (ModernElement<?, Canvas> elementLike : elements.values()) {
                handleElement(elementLike);
            }
        }

        private void handleElement(@NotNull ModernElement<?, Canvas> elementLike) {
            Vec2i vecPos = PropertyHelper.vectorOrThrow(elementLike.position(), Vec2i.class);
            Vec2i layoutVecPos = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
            elementLike.position(Position.fixed(vecPos.add(layoutVecPos)).target(elementLike.position().target()));
            if (!(elementLike instanceof GenericInteractableModernElement<?,?,?> interactableModernElement)) return;
            interactableModernElement.inputHandler(handler);
        }

        @Override
        public @Nullable ModernElement<?, Canvas> child(@NotNull Identifier identifier) {
            return elements.get(identifier);
        }

        @Override
        public @NotNull Layout<E> addUnchecked(ModernElement<?, Canvas> element) {
            add((E) element);
            handleElement(element);
            return this;
        }

        @Override
        public @NotNull Collection<ModernElement<?, Canvas>> children() {
            return elements.values();
        }

        @Override
        public @NotNull <L extends ModernElement<L, Canvas>> Layout<E> add(@NotNull L elementLike) {
            elements.put(elementLike.identifier(), elementLike);
            return this;
        }

        @Override
        public @NotNull <L extends ModernElement<L, Canvas>> Layout<E> add(@NotNull Iterable<@NotNull L> elementLikes) {
            for (L elementLike : elementLikes) {
                elements.put(elementLike.identifier(), elementLike);
            }
            for (ModernElement<?, Canvas> elementLike : elementLikes) {
                handleElement(elementLike);
            }
            return this;
        }

        @Override
        public @NotNull <L extends ModernElement<L, Canvas>> Layout<E> remove(@NotNull L elementLike) {
            elements.remove(elementLike);
            return this;
        }

        @Override
        public @NotNull ElementContainer<@NotNull Layout<E>> remove(@NotNull Identifier identifier) {
            elements.remove(identifier);
            //handler.unlink(identifier);
            return this;
        }

        @Override
        public @NotNull Identifier identifier() {
            return identifier;
        }

        @Override
        public <T, C, P extends Property<T, C>> @NotNull P property(@NotNull Class<P> propertyClass) {
            return (P) propertyMap.get(propertyClass);
        }

        @Override
        public @NotNull <T, C> Layout<E> property(@NotNull Property<T, C> property) {
            propertyMap.put((Class<? extends Property<?, ?>>) property.getClass(), property);
            return this;
        }

        @Override
        public @NotNull Collection<Property<?, ?>> properties() {
            return List.of();
        }

        @Override
        public @NotNull Canvas render(@NonNull Size property, @Nullable RenderContext context) {
            for (ModernElement<?, Canvas> value : elements.values()) {
                value.visibility(visibility());
            }
            Vec2i calculatedSize = PropertyHelper.vectorOrThrow(size(), Vec2i.class);
            Canvas finalCanvas = Canvas.empty(calculatedSize);
            Vec2i layoutPosVec = PropertyHelper.vectorOrThrow(position(), Vec2i.class);
            for (ModernElement<?, Canvas> value : elements.values()) {
                Vec2i posVec = PropertyHelper.vectorOrThrow(value.position(), Vec2i.class);
                finalCanvas.place(value.render(property, context), posVec.sub(layoutPosVec));
            }
            return finalCanvas;
        }

        public @NotNull InputHandler handler() {
            return handler;
        }

    }

}
