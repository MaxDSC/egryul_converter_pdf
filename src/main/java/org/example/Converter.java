package org.example;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
    private PDDocument doc;
    private int countPage;
    private static final float A4_WIDTH = 210;
    private final float WIDTH_FIRST_COL;
    private final float WIDTH_TWO_COL;
    ArrayList<String> text;
    ArrayDeque<String> rows;


    public Converter(String pathName) throws IOException {
        doc = Loader.loadPDF(new File(pathName));
        countPage = doc.getNumberOfPages();
        WIDTH_FIRST_COL = 31;
        WIDTH_TWO_COL = 113.4f;
        rows = new ArrayDeque<String>();
    }

    public PDPage getPage(int number) {
        if (number > countPage || number < 0) throw new IllegalArgumentException("A non-existent page is specified");
        return doc.getPage(number);
    }

    public String getText() throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        return pdfStripper.getText(doc);
    }

    public void splitTextToRowsTable(int startNumberPage, int endNumberPage) throws IOException {
        if (startNumberPage < 0 || startNumberPage > countPage || endNumberPage < 0 || endNumberPage > countPage || endNumberPage < startNumberPage)
            throw new IllegalArgumentException("A non-existent page is specified");
        for (int i = startNumberPage; i <= endNumberPage; i++) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i);
            String text = pdfStripper.getText(doc);
            extractTable(text, rows);
        }
        for (String s : rows) System.out.println(s);
    }

    public void splitTextToRowsTable() throws IOException {
       splitTextToRowsTable(1, 17);
    }

    //Превратит исходный текст, который заключен в таблице PDF в нумерованные строки таблицы очереди.
    private void extractTable(String text, ArrayDeque<String> list) {
        String[] arrText = text.split("\r\n");
        int endTable = getLowestBorerTable(arrText);
        identifyNumberedLines(list, arrText);
        concatAssociatedStrings(list, arrText, endTable);
        fixNumbering(list);
    }

    //Вернет последнюю строку в стаблице
    private int getLowestBorerTable(String[] text) {
        int endTable = text.length - 3;
        for (int i = 0; i < text.length; i++) {
            if (text[i].equals("Выписка из ЕГРЮЛ")) {
                endTable = i - 1;
                break;
            }
        }
        return endTable;
    }

    //Вставляет нумерованные строчки из таблицы выписки в очередь
    // Если очередь не пустая, при обработке более одной страницы, то вставляет строчки из большей страницы вперед
    //Например, для одной страницы (очередь пуста) -> 1,2,3,4,5,6,7,8,9
    //Например, для двух страниц -> 10,11,12,13,14,15,1,2,3,4,5,6,7,8,9
    private void identifyNumberedLines(ArrayDeque<String> rows, String[] text) {
        int sizeOld = rows.size();
        boolean isNotEmpty = sizeOld > 0;
        Pattern pattern = Pattern.compile("^\\d{1,4}\\s\\D{1,}");

        for (int i = 0; i < text.length; i++) {
            Matcher matcher = pattern.matcher(text[i]);
            if (matcher.find()) rows.add(text[i]);
        }

        if (isNotEmpty) {
            for (int i = 0; i < sizeOld; i++) {
                rows.add(Objects.requireNonNull(rows.poll()));
            }
        }
    }

    //Соединяет все строки, которое находятся внутри одной нумерованной строки таблицы, но имеют перенос
    //Например, без ассоциации, ниже
    //1 Полное наименование на русском языке ОБЩЕСТВО С ОГРАНИЧЕННОЙ (в PDF одна строка)
    //ОТВЕТСТВЕННОСТЬЮ "Дельта"
    //2 ГРН и дата внесения в ЕГРЮЛ записи,
    //содержащей указанные сведения
    //1000000000000
    //11.12.2002
    //С ассоциацией превратиться в ->
    //1 Полное наименование на русском языке ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ "Дельта"
    //2 ГРН и дата внесения в ЕГРЮЛ записи, содержащей указанные сведения 1000000000000 11.12.2002
    private void concatAssociatedStrings(ArrayDeque<String> rows, String[] text, int endTable) {
        for (int i = 0; i < endTable; i++) {
            String str = text[i];
            if (str.equals(rows.getFirst())) {
                int j = 1;
                String s = rows.poll();
                while (j < endTable - i && !text[i + j].equals(rows.getFirst())) {
                    s = s + " " + text[i + j];
                    j++;
                }
                rows.add(s);
            }
        }
    }

    //Убирает строки, которые нарушают нумерацию строчек.
    //Обычно это даты, которые распознались как номер строки, например, 1 июля - будет распознано, как отдельная строка таблицы
    // 15  Наименование органа, зарегистрировавшего юридическое лицо до ...
    // 1 июля 2002 года ...
    // 16 ГРН и дата внесения в ЕГРЮЛ записи, содержащей указанные сведения ...
    //Превратит в
    // 15  Наименование органа, зарегистрировавшего юридическое лицо до 1 июля 2002 года ...
    // 16 ГРН и дата внесения в ЕГРЮЛ записи, содержащей указанные сведения ...
    //Данный метод соединяет предыдущую строку с ошибочной строкой.
    private void fixNumbering(ArrayDeque<String> rows) {
        Pattern pattern = Pattern.compile("^\\d{1,4}\\s");
        int lastNum = 0;
        for (int i = 0; i < rows.size(); i++) {
            String str = rows.poll();
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                String find = str.substring(matcher.start(), matcher.end());
                int num = Integer.parseInt(find.replace(" ", ""));
                if (lastNum != num - 1) {
                    rows.add(rows.pollLast() + " " + str);
                    i--;
                } else {
                    lastNum = num;
                    rows.add(str);
                }
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

    public ArrayDeque<String> getRows() {
        return rows;
    }
}
