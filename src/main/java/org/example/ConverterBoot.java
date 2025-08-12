package org.example;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class ConverterBoot {
    public static void main(String[] args) {
        //TODO: в будущем путь до файла надо брать из args
        try {
            Converter converter = new Converter("C:/Users/Programmer/res/vipiska.pdf");
            ArrayDeque<String> rows = converter.splitTextToRowsTable(1,3);
            ArrayList<String> templates = converter.getTemplates();
//            for(String s : templates) System.out.println(s);
//            for (String s : rows) System.out.println(s);
            converter.findJsonKeys();
           //System.out.println(converter.makeRegularExp("ОГРН%1%2;ОГРН; \\s[0-9]{12};[0-9]{1}".split(";"))[1]);

//            System.out.println(text);
//            float h = converter.findUpperBorderText(1);
//            System.out.println("Border Y="+h);
//            System.out.println(converter.getTextFromArea(1, 55));
            //ArrayList<String> lines = converter.extractText(1);
//            converter.extractText();
//            for(String s : converter.allLines) System.out.print(s);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void jsonOperations() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        try {
            Vypiska vypiska = mapper.readValue(new File("C:/Users/Programmer/res/vip.json"), Vypiska.class);
            System.out.println(vypiska.toString());
            Class<Vypiska> cls = Vypiska.class;
            Field[] arrFields = cls.getDeclaredFields();
            JsonSetter ann = arrFields[0].getAnnotation(JsonSetter.class);
            System.out.println(ann.value());
            Vypiska set = new Vypiska();
            set.INN = "0434344344";
            String json = mapper.writeValueAsString(set);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
