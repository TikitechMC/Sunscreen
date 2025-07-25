package me.combimagnetron.sunscreen.resourcepack;

import me.combimagnetron.sunscreen.resourcepack.feature.font.FontFeature;
import me.combimagnetron.sunscreen.resourcepack.feature.overlay.OverlayFeature;
import me.combimagnetron.sunscreen.resourcepack.feature.shader.ShaderFeature;

import java.util.Collection;
import java.util.List;

public interface Language {
    Language JSON = of("json", FontFeature.class, OverlayFeature.class);
    Language GLSL = of("glsl", ShaderFeature.class);

    String language();

    Collection<Class<? extends ResourcePackFeature<?, ?>>> applicableTo();

    Collection<String> fileExtensions();

    static Language of(String language, Class<? extends ResourcePackFeature<?, ?>>... applicableTo) {
        return new Impl(language, applicableTo);
    }

    record Impl(String language, Class<? extends ResourcePackFeature<?, ?>>... app) implements Language {

        @Override
        public Collection<Class<? extends ResourcePackFeature<?, ?>>> applicableTo() {
            return List.of(app);
        }

        @Override
        public Collection<String> fileExtensions() {
            return switch (language) {
                case "json" -> List.of("json");
                case "glsl" -> List.of("vsh", "fsh");
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };
        }
    }
}
