package org.example;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
    private PDDocument doc;
    private int countPage;
    private static final float A4_WIDTH = 210;
    private final float WIDTH_FIRST_COL;
    private final float WIDTH_TWO_COL;
    private final String SPLIT_TEMPLATE = ";";
    private final String MARK_REG_EXP = "%";
    private ArrayList<String> text;
    private ArrayDeque<String> rows;
    private ArrayList<String> templates;
    private Map<String, String> jsonKeys;


    /**
     * Конструктор конвертера. Сразу извлекает шаблоны строк из файла templates.csv.
     *
     * @param pathName - путь к файлу PDF-выписки.
     * @throws IOException        -неудачная операция с файлом выписки.
     * @throws URISyntaxException - неудачная операция с файлом шаблонов.
     */
    //TODO: Добавить проверку, что путь ведет именно к PDF-файлу
    public Converter(String pathName) throws IOException, URISyntaxException {
        doc = Loader.loadPDF(new File(pathName));
        countPage = doc.getNumberOfPages();
        WIDTH_FIRST_COL = 31;
        WIDTH_TWO_COL = 113.4f;
        rows = new ArrayDeque<String>();
        jsonKeys = new HashMap<>();
        setTemplates();
    }

    /**
     * Получить весь текст PDF-выписки с конкретной страницы.
     *
     * @param number - номер страницы
     * @return строку, в которой будет весь текст PDF-выписки.
     * @throws IllegalArgumentException - номер страницы больше максимальной или отрицательный.
     */
    public PDPage getPage(int number) {
        if (number > countPage || number < 0) throw new IllegalArgumentException("A non-existent page is specified");
        return doc.getPage(number);
    }

    /**
     * Получить весь текст PDF-выписки.
     *
     * @return строку, в которой будет весь текст PDF-выписки.
     */
    public String getText() throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        return pdfStripper.getText(doc);
    }

    /**
     * Создает очередь строк, которые представляют собой строку таблицы выписки PDF из диапазона страниц.
     *
     * @param startNumberPage страница, с которой начинается анализ.
     * @param endNumberPage   страница, на которой нужно остановить анализ.
     * @return очередь @{@link ArrayDeque}, в которой будут строки таблицы выписки.
     */
    public ArrayDeque<String> splitTextToRowsTable(int startNumberPage, int endNumberPage) throws IOException {
        if (startNumberPage < 0 || startNumberPage > countPage || endNumberPage < 0 || endNumberPage > countPage || endNumberPage < startNumberPage)
            throw new IllegalArgumentException("A non-existent page is specified");
        for (int i = startNumberPage; i <= endNumberPage; i++) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i);
            String text = pdfStripper.getText(doc);
            extractTable(text, rows);
        }
        return rows;
    }

    /**
     * Создает очередь строк, которые представляют собой строку таблицы выписки PDF из всех страниц.
     *
     * @return очередь @{@link ArrayDeque}, в которой будут строки таблицы выписки из всех страниц.
     */
    public ArrayDeque<String> splitTextToRowsTable() throws IOException {
        return splitTextToRowsTable(1, countPage);
    }

    /**
     * Выведет значение 3 столбца выписки (Значение показателя), согласно загруженным шаблонам.
     * Ищет в коллекции строк таблицы выписки строки по шаблонам из коллекции шаблонов. Шаблоны соответствуют значениям из второго столбца выписки.
     * Если строка из таблицы имеет подстроку из шаблона (или по регулярному выражению), то из строки таблицы вырезается шаблон, таким образом, оставляя только значение 3-ого столбца таблицы выписки.
     * При этом удаляется элемент шаблона из коллекции шаблонов, для анализа тех шаблонов, которые не удалось найти.
     */
    public void findJsonKeys() {
        if (rows == null || rows.isEmpty())
            throw new IllegalArgumentException("There is no data to search!");
        if (templates == null || templates.isEmpty())
            throw new IllegalArgumentException("Templates is null or empty!");
        for (String r : rows) {
            String findedT = "";
            for (String t : templates) {
                String[] arrT = t.split(SPLIT_TEMPLATE);
                boolean isRegex = containsRegExp(arrT);
                String[] twoExp = setRegexpInMarkedString(arrT);
                Pattern pattern = Pattern.compile(twoExp[0]);
                Matcher matcher = pattern.matcher(r);
                if (matcher.find()) {
                    int endSub = matcher.end();
                    if (isRegex) {
                        Pattern innerPatter = Pattern.compile(twoExp[1]);
                        Matcher innerMatcher = innerPatter.matcher(r);
                        if (innerMatcher.find()) endSub = innerMatcher.end();

                    }
                    String value = r.substring(endSub);
                    System.out.println("R---> " + r);
                    System.out.println("S---> " + twoExp[0]);
                    System.out.println("V--->" + value + "\n");
                    findedT = t;
                    break;
                }
            }
            templates.remove(findedT);
        }
        if (templates.isEmpty()) {
            System.out.println("All templates is found!");
        } else {
            System.out.println("Not founded templates:");
            for (String s : templates) System.out.println(s);
        }
    }

    /**
     * Содержит ли массив строк, собранный из строки шаблона регулярное выражение. Строка шаблона не имеет регулярного выражения, если содержит только два члена, идущих через точку с запятой.
     *
     * @param arrTemplate массив строк, полученный через split строки-шаблона.
     * @return true - если имеет регулярное выражение, false - если не имеет регулярного выражения.
     */
    private boolean containsRegExp(String[] arrTemplate) {
        return arrTemplate.length > 2;
    }

    /**
     * Подставить регулярное выражение в строку из arrTemplate[0], которая имеет формат "Строка%1", где %1 - это место подставки регулярного выражения.
     * Таких подстановок может быть до 9 штук (%1-%9), регулярные выражения соответствующие подстановкам начинаются со 2-ого индекса.
     * Метод подставит в %1 - arrTemplate[2], в %9 - arrTemplate[10], но если маркера подстановки нет (только 2 элемента массива), то вернет исходный arrTemplate[0].
     *
     * @param arrTemplate массив строк, полученный через split строки-шаблона.
     * @return массив из двух элементов, где 0 - строка с включенным в нее регулярным выражение, подставленным в %1-%9 arrTemplate[0], в 1 - это значение arrTemplate[0] без подстановки регулярного выражения (без %1-%9).
     * Если строка шаблона не имеет регулярного выражения, то элементы в массив одинаковы и равны строке шаблона.
     */
    public String[] setRegexpInMarkedString(String[] arrTemplate) {
        String sub = arrTemplate[0];
        String[] result = {sub, sub};
        if (containsRegExp(arrTemplate)) {
            StringBuilder sb = new StringBuilder(sub);
            StringBuilder strWithoutRegexp = new StringBuilder(sub);
            for (int i = 2; i < arrTemplate.length; i++) {
                Pattern pattern = Pattern.compile(MARK_REG_EXP + (i - 1));
                Matcher matcher = pattern.matcher(sb);
                if (matcher.find()) {
                    sb.replace(matcher.start(), matcher.end(), arrTemplate[i]);
                    int indexMARK_REG_EXP = strWithoutRegexp.indexOf(MARK_REG_EXP + (i - 1));
                    strWithoutRegexp.replace(indexMARK_REG_EXP, indexMARK_REG_EXP + 2, "");
                }
            }
            result[0] = sb.toString();
            result[1] = strWithoutRegexp.toString();
        }
        return result;
    }

    /**
     * Превратит исходный текст, который заключен в таблице выписки в очередь строк таблицы.
     *
     * @param text весь текст из PDF-выписки.
     * @param rows очередь @{@link ArrayDeque}, в которую загружаются строки таблицы.
     */
    private void extractTable(String text, ArrayDeque<String> rows) {
        String[] arrText = text.split("\r\n");
        int endTable = getLowestBorerTable(arrText);
        identifyNumberedLines(rows, arrText);
        concatAssociatedStrings(rows, arrText, endTable);
        fixNumbering(rows);
    }


    /**
     * Вернет последнюю строку в таблице выписки.
     *
     * @param textArr массив из всего текста PDF-выписки, но разделенный функцией split по переносу строки.
     */
    private int getLowestBorerTable(String[] textArr) {
        int endTable = textArr.length - 3;
        for (int i = 0; i < textArr.length; i++) {
            if (textArr[i].equals("Выписка из ЕГРЮЛ")) {
                endTable = i - 1;
                break;
            }
        }
        return endTable;
    }


    /**
     * Вставляет нумерованные строчки из таблицы выписки в очередь.
     * Если очередь не пустая, при обработке более одной страницы, то вставляет строчки из большей страницы вперед.
     * <div><pre>
     * Например, для одной страницы (очередь пуста) -&gt; 1,2,3,4,5,6,7,8,9
     * Например, для двух страниц -&gt; 10,11,12,13,14,15,1,2,3,4,5,6,7,8,9
     * </pre></div>
     *
     * @param rows    очередь @{@link ArrayDeque}, в которую загружаются строки таблицы.
     * @param textArr массив из всего текста PDF-выписки, но разделенный функцией split по переносу строки.
     */
    private void identifyNumberedLines(ArrayDeque<String> rows, String[] textArr) {
        int sizeOld = rows.size();
        boolean isNotEmpty = sizeOld > 0;
        Pattern pattern = Pattern.compile("^\\d{1,4}\\s\\D{1,}");

        for (int i = 0; i < textArr.length; i++) {
            Matcher matcher = pattern.matcher(textArr[i]);
            if (matcher.find()) rows.add(textArr[i]);
        }

        if (isNotEmpty) {
            for (int i = 0; i < sizeOld; i++) {
                rows.add(Objects.requireNonNull(rows.poll()));
            }
        }
    }


    /**
     * Соединяет все строки, которое находятся внутри одной нумерованной ячейки таблицы, но имеют перенос.
     * Например, без ассоциации, ниже
     * <div><pre>
     * 1 Полное наименование на русском языке ОБЩЕСТВО С ОГРАНИЧЕННОЙ
     * ОТВЕТСТВЕННОСТЬЮ "Дельта"
     * 2 ГРН и дата внесения в ЕГРЮЛ записи,
     * содержащей указанные сведения
     * 1000000000000
     * 11.12.2002
     * </pre></div>
     * С ассоциацией превратиться в
     * <div><pre>
     * 1 Полное наименование на русском языке ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ "Дельта"
     * 2 ГРН и дата внесения в ЕГРЮЛ записи, содержащей указанные сведения 1000000000000 11.12.2002
     * </pre></div>
     *
     * @param rows     очередь @{@link ArrayDeque}, в которую загружаются строки таблицы.
     * @param textArr  массив из всего текста PDF-выписки, но разделенный функцией split по переносу строки.
     * @param endTable - индекс последней строки из textArr, где заканчивается таблица на странице.
     */
    private void concatAssociatedStrings(ArrayDeque<String> rows, String[] textArr, int endTable) {
        for (int i = 0; i < endTable; i++) {
            String str = textArr[i];
            if (str.equals(rows.getFirst())) {
                int j = 1;
                String s = rows.poll();
                while (j < endTable - i && !textArr[i + j].equals(rows.getFirst())) {
                    s = s + " " + textArr[i + j];
                    j++;
                }
                rows.add(s);
            }
        }
    }


    /**
     * Убирает строки, которые нарушают нумерацию строчек.
     * Обычно это даты, которые были распознаны как номер строки, например, 1 июля - будет распознано, как отдельная строка таблицы. Данный метод соединяет предыдущую строку с ошибочной строкой.
     * Например,
     * <div><pre>
     * 15 Наименование органа, зарегистрировавшего юридическое лицо до ...
     * 1 июля 2002 года ...
     * 16 ГРН и дата внесения в ЕГРЮЛ записи, содержащей указанные сведения ...
     * </pre></div>
     * Превратит в
     * <div><pre>
     * 15 Наименование органа, зарегистрировавшего юридическое лицо до 1 июля 2002 года ...
     * 16 ГРН и дата внесения в ЕГРЮЛ записи, содержащей указанные сведения ...
     * </pre></div>
     *
     * @param rows очередь @{@link ArrayDeque}, в которую загружаются строки таблицы.
     */
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

    /**
     * Считывает из файла templates.csv шаблоны поиска, которые должны соответствовать значениям ячеек во втором столбце PDF-выписки.
     */
    private void setTemplates() throws URISyntaxException, IOException {
        templates = new ArrayList<String>();
        URL res = getClass().getClassLoader().getResource("templates.csv");
        File fileTemplates = Paths.get(res.toURI()).toFile();
        BufferedReader br = new BufferedReader(new FileReader(fileTemplates, StandardCharsets.UTF_8));
        String s;
        while ((s = br.readLine()) != null) {
            templates.add(s);
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

    public ArrayList<String> getTemplates() {
        return templates;
    }

    public void setTemplates(ArrayList<String> templates) {
        this.templates = templates;
    }

}
