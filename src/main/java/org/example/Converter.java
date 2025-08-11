package org.example;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
    private PDDocument doc;
    private int countPage;
    private static final float A4_WIDTH = 210;
    private final float WIDTH_FIRST_COL;
    private final float WIDTH_TWO_COL;
    ArrayList<String> text;


    public Converter(String pathName) throws IOException {
        doc = Loader.loadPDF(new File(pathName));
        countPage = doc.getNumberOfPages();
        WIDTH_FIRST_COL = 31;
        WIDTH_TWO_COL = 113.4f;
    }

    public PDPage getPage(int number) {
        if (number > countPage || number < 0) throw new IllegalArgumentException("A non-existent page is specified");
        return doc.getPage(number);
    }

    public String getText() throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        return pdfStripper.getText(doc);
    }

    public String getText(int startNumberPage, int endNumberPage) throws IOException {
        if (startNumberPage < 0 || startNumberPage > countPage || endNumberPage < 0 || endNumberPage > countPage || endNumberPage < startNumberPage)
            throw new IllegalArgumentException("A non-existent page is specified");

        ArrayDeque<String> rows = new ArrayDeque<String>();
        if (startNumberPage == endNumberPage) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(startNumberPage);
            pdfStripper.setEndPage(startNumberPage);
            String text = pdfStripper.getText(doc);
            extractTable(text, rows);
        } else {
            for (int i = startNumberPage; i <= endNumberPage; i++) {
                PDFTextStripper pdfStripper = new PDFTextStripper();
                pdfStripper.setStartPage(i);
                pdfStripper.setEndPage(i);
                String text = pdfStripper.getText(doc);
                extractTable(text, rows);
            }

        }
        for (String s : rows) System.out.println(s);


//        String[] arrText = text.split("\r\n");
//        System.out.println("SIZE="+arrText.length);
//        int endTable  = arrText.length - 3;
//        for(int i = 0; i< arrText.length; i++){
//            if (arrText[i].equals("Выписка из ЕГРЮЛ")){
//                endTable = i - 1;
//            }
//        }
//        System.out.println("Index="+endTable);
//        Pattern pattern = Pattern.compile("^\\d{1,3}\\s\\D{1,}");
//        ArrayDeque<String> list = new ArrayDeque<>();
//        for(int i = 0 ; i < arrText.length; i++){
//            Matcher matcher = pattern.matcher(arrText[i]);
//            if(matcher.find()) list.add(arrText[i]);
//        }
//
//        for(int i = 0; i < endTable; i++){
//            String str = arrText[i];
//            if (str.equals(list.getFirst())){
//                int j = 1;
//                String s = list.poll();
//                while(j < endTable - i && !arrText[i + j].equals(list.getFirst())){
//                    s = s + " " + arrText[i+j];
//                    j++;
//                }
//                list.add(s);
//            }
//        }
//
//        for(String s : list) System.out.println(s);

        return "pdfStripper.getText(doc);";
    }

    private void extractTable(String text, ArrayDeque<String> list) {
        int sizeOld = list.size();
        boolean isNotEmpty = sizeOld > 0;
        String[] arrText = text.split("\r\n");
        int endTable = arrText.length - 3;
        Pattern pattern = Pattern.compile("^\\d{1,3}\\s\\D{1,}");

        for (int i = 0; i < arrText.length; i++) {
            if (arrText[i].equals("Выписка из ЕГРЮЛ")) {
                endTable = i - 1;
                break;
            }
        }

        for (int i = 0; i < arrText.length; i++) {
            Matcher matcher = pattern.matcher(arrText[i]);
            if (matcher.find()) list.add(arrText[i]);
        }

        if (isNotEmpty) {
            for (int i = 0; i < sizeOld; i++) {
                list.add(Objects.requireNonNull(list.poll()));
            }
        }

        for (int i = 0; i < endTable; i++) {
            String str = arrText[i];
            if (str.equals(list.getFirst())) {
                int j = 1;
                String s = list.poll();
                while (j < endTable - i && !arrText[i + j].equals(list.getFirst())) {
                    s = s + " " + arrText[i + j];
                    j++;
                }
                list.add(s);
            }
        }
    }

    public String getTextFromArea(int numberPage) throws IOException {
        numberPage = numberPage - 1;
        if (numberPage > countPage || numberPage < 0)
            throw new IllegalArgumentException("A non-existent page is specified");
        PDPage page = getPage(numberPage);
        PDRectangle pageRect = page.getBBox();
        float width = pageRect.getWidth();
        PDRectangle rect = new PDRectangle(0, 0, width, 100);
        System.out.println(rect.toString());
        PDFTextStripperByArea pdfArea = new PDFTextStripperByArea();
        pdfArea.addRegion("reg1", rect.toGeneralPath().getBounds2D());
        pdfArea.extractRegions(page);
        String textArea = pdfArea.getTextForRegion("reg1");
        return textArea;
    }

    public String getTextFromArea(int numberPage, float h) throws IOException {
        numberPage = numberPage - 1;
        if (numberPage > countPage || numberPage < 0)
            throw new IllegalArgumentException("A non-existent page is specified");
        PDPage page = getPage(numberPage);
        PDRectangle pageRect = page.getBBox();
        float width = pageRect.getWidth();
        PDRectangle rect = new PDRectangle(0, 50, width, 5);
        System.out.println(rect.toString());
        PDFTextStripperByArea pdfArea = new PDFTextStripperByArea();
        pdfArea.addRegion("reg1", rect.toGeneralPath().getBounds2D());
        pdfArea.extractRegions(page);
        String textArea = pdfArea.getTextForRegion("reg1");
        return textArea;
    }

    public float findUpperBorderText(int numberPage) throws IOException {
        numberPage = numberPage - 1;
        if (numberPage > countPage || numberPage < 0)
            throw new IllegalArgumentException("A non-existent page is specified");
        PDPage page = doc.getPage(numberPage);
        PDRectangle pageRect = page.getBBox();
        float x = 0;
        float y = 0;
        float width = pageRect.getWidth();
        float height = 0.5F;
        PDFTextStripperByArea pdfArea = new PDFTextStripperByArea();
        while (true) {
            PDRectangle rect = new PDRectangle(x, y, width, height);
            System.out.println(rect.toString());
            pdfArea.addRegion("reg1", rect.toGeneralPath().getBounds2D());
            pdfArea.extractRegions(page);
            String textArea = pdfArea.getTextForRegion("reg1");
            if (!textArea.isEmpty() && !textArea.equals("\n") && !textArea.equals("\r\n")) {
                break;
            }
            height = height + 0.5f;
        }
        return height;
    }

    public void extractText() throws IOException {
        for (int numberPage = 0; numberPage < countPage; numberPage++) {
            PDPage page = doc.getPage(numberPage);
            PDRectangle pageRect = page.getBBox();
            text = new ArrayList<String>();
            float yMax = pageRect.getUpperRightY();
            float width = pageRect.getWidth();
            float middle = pageRect.getUpperRightX() * (WIDTH_TWO_COL / A4_WIDTH - 0.005f);
            PDFTextStripperByArea pdfArea = new PDFTextStripperByArea();
            float y = 0;
            float height = 0.5F;
            while (true) {
                if (height >= yMax - y) break;
                height = 0.5F;
                while (height < yMax - y) {
                    PDRectangle rect = new PDRectangle(0, y, width, height);
                    System.out.println(rect.toString() + "h=" + height);
                    pdfArea.addRegion("reg1", rect.toGeneralPath().getBounds2D());
                    pdfArea.extractRegions(page);
                    String textArea = pdfArea.getTextForRegion("reg1");
                    if (!textArea.isEmpty() && !textArea.equals("\n") && !textArea.equals("\r\n")) {
                        y = y + height;
                        text.add(textArea);
                        break;
                    }
                    height = height + 0.5f;
                }
            }
        }

    }

    public ArrayList<String> extractText(int numberPage) throws IOException {
        numberPage = numberPage - 1;
        if (numberPage > countPage || numberPage < 0)
            throw new IllegalArgumentException("A non-existent page is specified");
        ArrayList<String> lines = new ArrayList<String>();
        PDPage page = doc.getPage(numberPage);
        PDRectangle pageRect = page.getBBox();
        float yMax = pageRect.getUpperRightY();
        float width = pageRect.getWidth();
        float middle = pageRect.getUpperRightX() * (WIDTH_TWO_COL / A4_WIDTH - 0.005f);
        PDFTextStripperByArea pdfArea = new PDFTextStripperByArea();
        float y = 0;
        float height = 0.5F;
        while (true) {
            if (height >= yMax - y) break;
            height = 0.5F;
            while (height < yMax - y) {
                PDRectangle rect = new PDRectangle(0, y, width, height);
                System.out.println(rect.toString() + "h=" + height);
                pdfArea.addRegion("reg1", rect.toGeneralPath().getBounds2D());
                pdfArea.extractRegions(page);
                String textArea = pdfArea.getTextForRegion("reg1");
                if (!textArea.isEmpty() && !textArea.equals("\n") && !textArea.equals("\r\n")) {
                    y = y + height;
                    lines.add(textArea);
                    break;
                }
                height = height + 0.5f;
            }
        }
        return lines;
    }


    public void closeDocument() throws IOException {
        doc.close();
    }

    public PDDocument getDoc() {
        return doc;
    }

    public int getCountPage() {
        return countPage;
    }
}
