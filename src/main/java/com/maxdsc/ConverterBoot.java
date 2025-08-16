package com.maxdsc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class ConverterBoot {
    public static void main(String[] args) {
        //TODO: в будущем путь до файла надо брать из args
        try {
            String pathFile = "C:/Users/Programmer/res/vipiska.pdf";
            Vypiska vypiska = new Vypiska(pathFile);
            vypiska.convert();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void testTree(String pathFile) throws IOException {
        Vypiska vypiska = new Vypiska(pathFile);
        Section sec1 = new Section("Секция 1");
        Section sec2 = new Section("Секция 2");
        Section sec3 = new Section("Секция 3");
        Section subSec1 = new Section("Подсекция 1");
        Section subSec2 = new Section("Подсекция 1");
        Section subSec1Lev2 = new Section("Подсекция подсекции");

        sec1.addSubSection(subSec1);
        subSec1Lev2.addSubSection(new Section("Под-под-под секция"));
        subSec2.addSubSection(subSec1Lev2);
        sec2.addSubSection(subSec2);
        sec2.addSubSection(new Section("Подсекция 2"));

        ArrayDeque<String> text1 = new ArrayDeque<>();
        text1.add("пример строки таблицы 1");
        text1.add("пример строки таблицы 2");
        text1.add("пример строки таблицы 3");
        sec1.setRows(text1);
        subSec2.setRows(text1);

        ArrayDeque<Section> allSec = new ArrayDeque<>();
        allSec.add(sec1);
        allSec.add(sec2);
        allSec.add(sec3);
        vypiska.setSections(allSec);
        System.out.println(vypiska.getTree());
    }

    public static void Old() {
        try {
            Converter converter = new Converter("C:/Users/Programmer/res/vipiska.pdf");
            ArrayDeque<String> rows = converter.splitTextToRowsTable(1, 10);
            ArrayList<String> templates = converter.getTemplates();
            for (String s : rows) System.out.println(s);

            converter.findJsonKeys();
            HashMap<String, String> jsonKeys = converter.getJsonKeys();
            System.out.println("JSON SIZE=" + jsonKeys.size());
            VypiskaJSON vypiskaJSON = new VypiskaJSON(jsonKeys);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(vypiskaJSON);
            System.out.println(json);
            converter.closeDocument();

        } catch (IOException | URISyntaxException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
