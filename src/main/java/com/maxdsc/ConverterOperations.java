package com.maxdsc;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class ConverterOperations implements Convertible {
    private PDDocument doc;
    private int countPage;
    private final static int DROPPED_END_LINES = 3;
    private final static String SEPARATOR = "\r\n";
    private final int INIT_COUNT_LINES_IN_PAGE = 55;
    private final int INIT_COUNT_TEMPLATES = 100;
    private final String FILE_TEMPLATE = "table_template.csv";


    public ConverterOperations(PDDocument doc) {
        this.doc = doc;
        this.countPage = doc.getNumberOfPages();
    }

    @Override
    public String getText() throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        return pdfStripper.getText(doc);
    }

    @Override
    public ArrayDeque<String> textToCollect() throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        ArrayDeque<String> text = new ArrayDeque<>(INIT_COUNT_LINES_IN_PAGE * countPage);
        for (int i = 1; i <= countPage; i++) {
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i);
            String[] pageText = pdfStripper.getText(doc).split(SEPARATOR);
            for (int j = 0; j < pageText.length - DROPPED_END_LINES + 3; j++) {
                text.add(pageText[j]);
            }
        }
        return text;
    }

    @Override
    public ArrayList<String> getTemplate() throws URISyntaxException, IOException {
        ArrayList<String> templates = new ArrayList<>(INIT_COUNT_TEMPLATES);
        URL res = getClass().getClassLoader().getResource(FILE_TEMPLATE);
        if (res != null) {
            Path path = Paths.get(res.toURI());
            templates = (ArrayList<String>) Files.readAllLines(path, StandardCharsets.UTF_8);
        }
        return templates;
    }
}
