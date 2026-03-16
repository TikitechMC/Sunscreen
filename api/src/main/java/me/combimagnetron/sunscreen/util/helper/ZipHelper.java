package me.combimagnetron.sunscreen.util.helper;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Enumeration;

public class ZipHelper {

    private ZipHelper() {}

    public static @NotNull Path unzip(@NotNull Path file) {
        String destination = file.toString().replace(".zip", "");
        try {
            ZipFile zipFile = ZipFile.builder().setFile(new File("archive.zip")).get();
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.getName().startsWith("..")) continue;
                File outFile = new File(destination, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = new FileOutputStream(outFile)) {
                        IOUtils.copy(in, out);
                    }
                }
            }
            zipFile.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Path.of(destination);
    }

}
