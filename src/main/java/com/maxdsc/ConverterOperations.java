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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConverterOperations implements Convertible {
    private PDDocument doc;
    private int countPage;
    private final static int DROPPED_END_LINES = 3;
    private final static String END_LINE_SEPARATOR = "\r\n";
    private final int INIT_COUNT_LINES_IN_PAGE = 55;
    private final int INIT_COUNT_TEMPLATES = 100;
    private final String FILE_TEMPLATE = "table_template.csv";
    private final int MAX_NUMERIC = 100;
    private final String CSV_SEPARATOR = ";";
    private final String MARK_SECTION = "Р";
    private final String MARK_SUBSECTION_NUMERIC = "П#";

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
            String[] pageText = pdfStripper.getText(doc).split(END_LINE_SEPARATOR);
            for (int j = 0; j < pageText.length - DROPPED_END_LINES; j++) {
                text.add(pageText[j]);
            }
        }
        return text;
    }

    @Override
    public ArrayDeque<String> getTemplate() throws URISyntaxException, IOException {
        ArrayDeque<String> templates = new ArrayDeque<>(INIT_COUNT_TEMPLATES);
        URL res = getClass().getClassLoader().getResource(FILE_TEMPLATE);
        if (res != null) {
            Path path = Paths.get(res.toURI());
            templates.addAll(Files.readAllLines(path, StandardCharsets.UTF_8));
        }
        return templates;
    }

    @Override
    public ArrayDeque<Section> fillSections(ArrayDeque<String> text, ArrayDeque<String> templates) throws Exception {
        if (text.isEmpty() || templates.isEmpty())
            throw new Exception("Deque text is empty or Deque template is empty!");
        //TODO: на данный момент позволяет извлечь только первый раздел и выводит его в консоль.
        ArrayDeque<String> copyText = new ArrayDeque<>(text);
        ArrayDeque<String> copyTemplates = new ArrayDeque<>(templates);
        ArrayDeque<String> templateInSections = defineSectionInTemplates(copyTemplates);
        for (String s : templateInSections) System.out.println(s);
        Section sec1 = fillOneSection(copyText, templateInSections);
        System.out.println("---------");
        System.out.println(sec1.getTree(""));
        // for (String s : sec1.getRows()) System.out.println(s);
        return null;
    }

    @Override
    public ArrayDeque<String> defineSectionInTemplates(ArrayDeque<String> allTemplates) {
        ArrayDeque<String> section = new ArrayDeque<>();
        do {
            section.add(allTemplates.poll());
        } while (!allTemplates.isEmpty() && allTemplates.peek().charAt(0) != MARK_SECTION.charAt(0));
        if (!allTemplates.isEmpty()) section.add(allTemplates.peek());
        return section;
    }

    @Override
    public Section fillOneSection(ArrayDeque<String> text, ArrayDeque<String> sectionTemplate) throws Exception {
        //TODO: добавить обработку подразделов и последнего раздела. На данный момент работает только для sectionTemplate размером 2 и для нумерованных подразделов.
        Section section = new Section();
        String[] topTemplate = sectionTemplate.poll().split(CSV_SEPARATOR);
        String[] nextTemplate = sectionTemplate.poll().split(CSV_SEPARATOR);
        if (sectionTemplate.isEmpty()) {
            while (checkNextTemplate(text, topTemplate[1])) text.poll();
            section.setName(text.poll());
            while (checkNextTemplate(text, nextTemplate[1])) {
                section.addRows(text.poll());
            }
        } else {
            if (nextTemplate[0].equals(MARK_SUBSECTION_NUMERIC)) {
                while (!text.isEmpty() && !topTemplate[1].equals(text.peek())) text.poll();
                section.setName(text.poll());
                section.setSubsections(getNumericSubsections(text, sectionTemplate.peek()));
            }
        }
        return section;
    }

    @Override
    public ArrayDeque<Section> getNumericSubsections(ArrayDeque<String> text, String afterNumericTemplate) throws Exception {
        ArrayDeque<String> numericArr = IntStream.rangeClosed(1, MAX_NUMERIC).mapToObj(String::valueOf).collect(Collectors.toCollection(ArrayDeque<String>::new));
        if (!text.peek().equals(numericArr.peek()))
            throw new Exception("Templates error: after the line #П there should be a numbered subsection. ");
        String strNextTemplate = afterNumericTemplate.split(CSV_SEPARATOR)[1];

        ArrayDeque<Section> numericSubsections = new ArrayDeque<>();
        while (checkNextTemplate(text, strNextTemplate)) {
            if (!numericArr.isEmpty() && numericArr.poll().equals(text.peek())) {
                Section sub = new Section(text.poll());
                while (checkNextTemplate(text, strNextTemplate) && !numericArr.peek().equals(text.peek())) {
                    sub.addRows(text.poll());
                }
                numericSubsections.add(sub);
            }
        }
        return numericSubsections;
    }

    @Override
    public boolean checkNextTemplate(ArrayDeque<String> text, String nextTemplate) {
        return !text.isEmpty() && !text.peek().equals(nextTemplate);
    }
}
