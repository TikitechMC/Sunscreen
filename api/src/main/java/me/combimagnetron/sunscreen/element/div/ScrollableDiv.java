package me.combimagnetron.sunscreen.element.div;

import me.combimagnetron.sunscreen.image.Canvas;
import me.combimagnetron.sunscreen.element.Element;
import me.combimagnetron.sunscreen.element.SimpleBufferedElement;
import me.combimagnetron.sunscreen.menu.RuntimeDefinableGeometry;
import me.combimagnetron.sunscreen.menu.input.InputHandler;
import me.combimagnetron.sunscreen.user.SunscreenUser;
import me.combimagnetron.sunscreen.util.*;

public non-sealed interface ScrollableDiv extends Div<Canvas> {

    ScrollableDiv scroll(double percentage);

    ScrollableDiv scroll(int slot);

    ScrollableDiv visibleSize(Vec2i size);

    Vec2i visibleSize();

    class Impl extends Div.Impl implements ScrollableDiv {
        private final static double Multiplier = 0.1;
        private Vec2i visibleSize = size();
        private int lastSlot = 0;
        private double scroll = 0.0;

        public Impl(Identifier identifier) {
            super(identifier);
        }

        @Override
        public ScrollableDiv scroll(double percentage) {
            this.scroll = percentage;
            return this;
        }

        @Override
        public ScrollableDiv scroll(int slot) {
            if (lastSlot == 8 && slot == 0) {
                this.scroll -= 1;
                System.out.println("Scroll: " + scroll + " Slot: " + slot);
                return this;
            }
            if (lastSlot == 0 && slot == 8) {
                this.scroll += 1;
                System.out.println("Scroll: " + scroll + " Slot: " + slot);
                return this;
            }
            if (lastSlot < slot) {
                this.scroll += 1;
                System.out.println("Scroll: " + scroll + " Slot: " + slot);
                return this;
            }
            if (lastSlot > slot) {
                this.scroll -= 1;
                System.out.println("Scroll: " + scroll + " Slot: " + slot);
                return this;
            }
            if (scroll < 0 || (visibleSize.y() + scroll) > size().y()) {
                scroll = 0;
            }
            System.out.println("Scroll: " + scroll + " Slot: " + slot);
            return this;
        }

        @Override
        public ScrollableDiv visibleSize(Vec2i size) {
            this.visibleSize = size;
            return this;
        }

        @Override
        public Vec2i visibleSize() {
            return visibleSize;
        }

        @Override
        public Canvas render(SunscreenUser<?> user) {
            Canvas canvas = canvas();
            int slot = (int) (scroll / Multiplier);
            lastSlot = slot;
            for (Element<Canvas> element : elements()) {
                for (RuntimeDefinable.Type<?, ?> definable : element.definables()) {
                    if (definable instanceof RuntimeDefinableGeometry.GeometryBuilder<?> geometry) {
                        element.geometry(geometry.finish(Pair.of(user.screenSize().pixel(), size())));
                    }
                }
                if (!hiddenElements().contains(element)) {
                    if (element.canvas() == null) {
                        continue;
                    }
                    canvas = canvas.place(element.canvas(), ViewportHelper.fromPosition(element.position()));

                }
            }
            if (visibleSize.add(Vec2i.of(0, (int) scroll)).y() > canvas.size().y() || scroll < 0) {
                scroll = 0;
            }
            System.out.println("Scroll: " + scroll);
            canvas = canvas.sub(visibleSize, Vec2i.of(0, (int) scroll));
            return canvas;
        }

    }

}
