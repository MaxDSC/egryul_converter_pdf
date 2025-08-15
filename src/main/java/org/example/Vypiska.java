package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Vypiska {

    public Vypiska() {
    }

    /**
     * Конструктор, заполняющий поля по значению в аннотации @{@link JsonSetter}, которые должны соответствовать ключу из параметра.
     *
     * @param jsonValues hashmap, где ключ - значение аннотации @{@link JsonSetter}, а значения - String для заполнения полей класса.
     */
    public Vypiska(HashMap<String, String> jsonValues) throws IllegalAccessException {
        Field[] fields = this.getClass().getDeclaredFields();
        ArrayList<String> jsonKeys = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            Annotation[] annotations = fields[i].getDeclaredAnnotations();
            for (int j = 0; j < annotations.length; j++) {
                if (annotations[j].annotationType().equals(JsonSetter.class)) {
                    String key = fields[i].getAnnotation(JsonSetter.class).value();
                    if (jsonValues.containsKey(key)) {
                        fields[i].set(this, jsonValues.get(key));
                    }
                    break;
                }
            }
        }
    }

    @JsonSetter("ИНН")
    String INN;
    @JsonSetter("КПП")
    String KPP;
    @JsonSetter("ОГРН")
    String OGRN;
    @JsonSetter("ДатаОГРН")
    String DataOGRN;
    @JsonSetter("ДатаРег")
    String DataReg;
    @JsonSetter("Статус")
    String Status;
    @JsonSetter("СпОбрЮЛ")
    String SpObrYuL;
    @JsonSetter("НаимСокрЮЛ")
    String NaimSokrYuL;
    @JsonSetter("НаимПолнЮЛ")
    String NaimPolnYuL;
    @JsonSetter("Адрес")
    Adres adres;

    @JsonGetter("ИНН")
    public String getINN() {
        return INN;
    }

    @JsonGetter("КПП")
    public String getKPP() {
        return KPP;
    }

    @JsonGetter("ОГРН")
    public String getOGRN() {
        return OGRN;
    }

    @JsonGetter("ДатаОГРН")
    public String getDataOGRN() {
        return DataOGRN;
    }

    @JsonGetter("ДатаРег")
    public String getDataReg() {
        return DataReg;
    }

    @JsonGetter("Статус")
    public String getStatus() {
        return Status;
    }

    @JsonGetter("СпОбрЮЛ")
    public String getSpObrYuL() {
        return SpObrYuL;
    }

    @JsonGetter("НаимСокрЮЛ")
    public String getNaimSokrYuL() {
        return NaimSokrYuL;
    }

    @JsonGetter("НаимПолнЮЛ")
    public String getNaimPolnYuL() {
        return NaimPolnYuL;
    }

    @JsonGetter("Адрес")
    public Adres getAdres() {
        return adres;
    }

    @Override
    public String toString() {
        return "Vypiska{" +
                "INN='" + INN + '\'' +
                ", KPP='" + KPP + '\'' +
                ", OGRN='" + OGRN + '\'' +
                ", DataOGRN=" + DataOGRN +
                ", DataReg=" + DataReg +
                ", Status='" + Status + '\'' +
                ", SpObrYuL='" + SpObrYuL + '\'' +
                ", NaimSokrYuL='" + NaimSokrYuL + '\'' +
                ", NaimPolnYuL='" + NaimPolnYuL + '\'' +
                ", adres=" + adres +
                '}';
    }

    static class Adres {
        @JsonSetter("КодРегион")
        int KodRegion;
        @JsonSetter("Индекс")
        int Indeks;
        @JsonSetter("АдресПолн")
        String AdresPoln;
        @JsonSetter("Дата")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate Data;

        @JsonGetter("КодРегион")
        public int getKodRegion() {
            return KodRegion;
        }

        @JsonGetter("Индекс")
        public int getIndeks() {
            return Indeks;
        }

        @JsonGetter("АдресПолн")
        public String getAdresPoln() {
            return AdresPoln;
        }

        @JsonGetter("Дата")
        public LocalDate getData() {
            return Data;
        }

        @Override
        public String toString() {
            return
                    "KodRegion=" + KodRegion +
                            ", Indeks=" + Indeks +
                            ", AdresPoln='" + AdresPoln + '\'' +
                            ", Data=" + Data +
                            '}';
        }
    }
}


