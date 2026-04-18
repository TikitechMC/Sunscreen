package me.combimagnetron.sunscreen.neo.file;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;

public class PackedMenu {
    public final static DocumentBuilder builder;

    static {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private final Document document;

    private PackedMenu() {
        this.document = builder.newDocument();
    }

    private PackedMenu(@NotNull Path path) throws IOException, SAXException {
        this.document = builder.parse(path.toFile());
    }

}
