package com.maxdsc;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.util.ArrayDeque;

public class ConverterOperations implements Convertible {
    private PDDocument doc;
    private int countPage;
    private final static int DROPPED_END_LINES = 3;
    private final static String SEPARATOR = "\r\n";


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
        ArrayDeque<String> text = new ArrayDeque<>(500);
        for (int i = 1; i <= 1; i++) {
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i);
            String[] pageText = pdfStripper.getText(doc).split(SEPARATOR);
            for (int j = 0; j < pageText.length - DROPPED_END_LINES + 3; j++) {
                text.add(pageText[j]);
            }
        }
        return text;
    }
}
