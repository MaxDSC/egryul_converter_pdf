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
    public ArrayDeque<Section> fillSections(ArrayDeque<String> text, ArrayDeque<String> templates) {
        if (text.isEmpty() || templates.isEmpty())
            throw new IllegalStateException("Deque text is empty or Deque template is empty!");
        //TODO: на данный момент позволяет извлечь только первый раздел и выводит его в консоль.
        ArrayDeque<String> copyText = new ArrayDeque<>(text);
        ArrayDeque<String> copyTemplates = new ArrayDeque<>(templates);
        ArrayDeque<String> templateInSections = defineSectionInTemplates(copyTemplates);
        for (String s : templateInSections) System.out.println(s);
        Section sec1 = fillOneSection(copyText, templateInSections);
        System.out.println("---------");
        for (String s : sec1.getRows()) System.out.println(s);
        return null;
    }

    @Override
    public ArrayDeque<String> defineSectionInTemplates(ArrayDeque<String> allTemplates) {
        ArrayDeque<String> section = new ArrayDeque<>();
        do {
            section.add(allTemplates.poll());
        } while (!allTemplates.isEmpty() && allTemplates.peek().charAt(0) != 'Р');
        if (!allTemplates.isEmpty()) section.add(allTemplates.peek());
        return section;
    }

    @Override
    public Section fillOneSection(ArrayDeque<String> text, ArrayDeque<String> sectionTemplate) {
        //TODO: добавить обработку подразделов и последнего раздела. На данный момент работает только для sectionTemplate размером 2.
        Section section = new Section();
        String[] topTemplate = sectionTemplate.poll().split(";");
        String[] nextTemplate = sectionTemplate.poll().split(";");
        if (sectionTemplate.isEmpty()) {
            while (!text.isEmpty() && !topTemplate[1].equals(text.peek())) text.poll();
            section.setName(text.poll());
            while ((!text.isEmpty() && !text.peek().equals(nextTemplate[1]))) {
                section.addRows(text.poll());
            }
        }
        return section;
    }
}
