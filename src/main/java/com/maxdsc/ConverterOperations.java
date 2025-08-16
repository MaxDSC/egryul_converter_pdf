package com.maxdsc;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class ConverterOperations implements  Convertible{
    private PDDocument doc;
    private int countPage;

    public ConverterOperations(PDDocument doc) {
        this.doc = doc;
        this.countPage = doc.getNumberOfPages();
    }

    @Override
    public String getText() throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        return pdfStripper.getText(doc);
    }
}
