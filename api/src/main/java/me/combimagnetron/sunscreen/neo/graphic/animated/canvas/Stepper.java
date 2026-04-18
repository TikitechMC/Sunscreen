package me.combimagnetron.sunscreen.neo.graphic.animated.canvas;

import org.jetbrains.annotations.NotNull;

public record Stepper(int frame) {

    public @NotNull Stepper increment() {
        return new Stepper(frame + 1);
    }

    public @NotNull Stepper target(int max) {
        return new Stepper(frame * (max/60));
    }

}
