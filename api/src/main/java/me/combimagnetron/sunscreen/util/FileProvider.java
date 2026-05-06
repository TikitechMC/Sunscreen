package me.combimagnetron.sunscreen.util;

import me.combimagnetron.sunscreen.SunscreenLibrary;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public interface FileProvider {

    File find(String path);

    static FileProvider resource() {
        return new ResourceFileProvider();
    }

    static FileProvider path() {
        return new PathFileProvider();
    }

    class ResourceFileProvider implements FileProvider {

        @Override
        public File find(String path) {
            File file;
            try {
                file = Files.createTempFile("sunscreen", ".tmp").toFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try (InputStream in = openClasspathResource(path);
                 OutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(Objects.requireNonNull(in, "Missing classpath resource: " + path), out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return file;
        }

        private static @Nullable InputStream openClasspathResource(@NotNull String path) {
            SunscreenLibrary<?, ?, ?> lib = SunscreenLibrary.library();
            if (lib != null) {
                InputStream fromLib = lib.resource(path);
                if (fromLib != null) {
                    return fromLib;
                }
            }
            ClassLoader ctx = Thread.currentThread().getContextClassLoader();
            if (ctx != null) {
                InputStream in = ctx.getResourceAsStream(path);
                if (in != null) {
                    return in;
                }
            }
            return SunscreenLibrary.class.getClassLoader().getResourceAsStream(path);
        }

    }

    class PathFileProvider implements FileProvider {

        @Override
        public File find(String path) {
            return SunscreenLibrary.library().path().resolve(path).toFile();
        }

    }

}
