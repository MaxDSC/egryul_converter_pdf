package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.time.LocalDate;

public class Vypiska {
    @JsonSetter("ИНН")
    String INN;
    @JsonSetter("КПП")
    String KPP;
    @JsonSetter("ОГРН")
    String OGRN;
    @JsonSetter("ДатаОГРН")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate DataOGRN;
    @JsonSetter("ДатаРег")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate DataReg;
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
    public LocalDate getDataOGRN() {
        return DataOGRN;
    }

    @JsonGetter("ДатаРег")
    public LocalDate getDataReg() {
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


