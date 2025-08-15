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
            Converter converter = new Converter("C:/Users/Programmer/res/vipiska.pdf");
            ArrayDeque<String> rows = converter.splitTextToRowsTable(1, 10);
            ArrayList<String> templates = converter.getTemplates();
            converter.findJsonKeys();
            HashMap<String, String> jsonKeys = converter.getJsonKeys();
            System.out.println("JSON SIZE=" + jsonKeys.size());
            Vypiska vypiska = new Vypiska(jsonKeys);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(vypiska);
            System.out.println(json);
            converter.closeDocument();

        } catch (IOException | URISyntaxException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
