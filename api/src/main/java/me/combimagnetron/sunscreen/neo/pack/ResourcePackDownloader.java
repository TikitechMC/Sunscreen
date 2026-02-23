package me.combimagnetron.sunscreen.neo.pack;

import me.combimagnetron.passport.util.data.Identifier;
import me.combimagnetron.passport.util.math.Vec2i;
import me.combimagnetron.sunscreen.SunscreenLibrary;
import me.combimagnetron.sunscreen.neo.graphic.Canvas;
import me.combimagnetron.sunscreen.neo.graphic.text.style.impl.font.AtlasFont;
import me.combimagnetron.sunscreen.neo.registry.Registries;
import me.combimagnetron.sunscreen.util.helper.ZipHelper;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creative.ResourcePack;
import team.unnamed.creative.font.BitMapFontProvider;
import team.unnamed.creative.font.Font;
import team.unnamed.creative.serialize.minecraft.MinecraftResourcePackReader;
import team.unnamed.creative.texture.Texture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ResourcePackDownloader {
    private static final Path CACHE = SunscreenLibrary.library().path().resolve(".cache", "pack.zip");

    public static void download(@NotNull String url) throws IOException {
        InputStream in;
        try {
            in = new URI(url).toURL().openStream();
            Files.copy(in, CACHE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | URISyntaxException e) {
            return;
        }
        Path unzipped = ZipHelper.unzip(CACHE);
        ResourcePack pack = MinecraftResourcePackReader.minecraft().readFromDirectory(unzipped.toFile());
        for (Font font : pack.fonts()) {
            Key fontFile = font.key();
            Identifier identifier = Identifier.of(fontFile.namespace(), fontFile.value());
            AtlasFont atlasFont = AtlasFont.font(identifier);
            for (BitMapFontProvider provider : font.providers().stream()
                    .filter(provider -> provider instanceof BitMapFontProvider)
                    .map(provider -> (BitMapFontProvider) provider).toList()) {
                Key textureLocation = provider.file();
                Texture texture = pack.texture(textureLocation);
                if (texture == null) throw new IOException("Texture does not exist");
                Canvas canvas = Canvas.data(new ByteArrayInputStream(texture.data().toByteArray()));
                List<String> characters = provider.characters();
                int widthSplit = characters.getFirst().length();
                int heightSplit = characters.size();
                Vec2i splitSize = canvas.size().div(widthSplit, heightSplit);
                int x = 0;
                int y = 0;
                for (String character : characters) {
                    for (char c : character.toCharArray()) {
                        Canvas sub = canvas.sub(Vec2i.of(x, y).mul(splitSize), splitSize);
                        y++;
                        atlasFont.register(c, sub);
                    }
                    x++;
                    y = 0;
                }
                Registries.register(Registries.FONTS, atlasFont);
            }
        }
    }

}
