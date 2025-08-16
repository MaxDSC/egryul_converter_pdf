package com.maxdsc;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;

public class Vypiska {
    private int countPage;
    private ArrayDeque<Section> sections;
    private Convertible convertibleOperations;


    public Vypiska(String pathName) throws IOException {
        PDDocument doc = Loader.loadPDF(new File(pathName));
        this.convertibleOperations = new ConverterOperations(doc);
    }

    /**
     * Получить строку дерева выписки.
     * Например, <div><pre>
     * ВЫПИСКА
     *   Секция 1
     *     пример строки таблицы 1
     *     пример строки таблицы 2
     *     пример строки таблицы 3
     *     Подсекция 1
     *   Секция 2
     *     Подсекция 1
     *       пример строки таблицы 1
     *       пример строки таблицы 2
     *       пример строки таблицы 3
     *       Подсекция подсекции
     *         Под-под-под секция
     *     Подсекция 2
     *   Секция 3
     * </pre></div>
     *
     * @return строку, представляющую собой все разделы выписки с подразделами, внутри раздела и каждого из подразделов могут быть строки текста.
     */
    public String getTree(){
        if (sections.isEmpty()) throw new NullPointerException("Sections is NULL!");
        String top = "ВЫПИСКА\n";
        for(Section sec : sections){
            top = top + sec.getTree("  ") + "\n";
        }
        return top;
    }

    public String convert() throws IOException {
        return convertibleOperations.getText();
    }

    public ArrayDeque<Section> getSections() {
        return sections;
    }

    public void setSections(ArrayDeque<Section> sections) {
        this.sections = sections;
    }
}
