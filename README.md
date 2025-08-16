# Экспериментальная версия исходного кода: Конвертер PDF выписки из выписки ЕГРЮЛ/ЕГРИП

### Исходный должен позволять извлекать данные из PDF выписки из ЕГРЮЛ/ЕГРИП и поместить их в JSON, такого формата, который используется для официального [API ФНС](https://api-fns.ru/api_help#section_dannye)

## Используемое ПО и библиотеки

- Java версия: openjdk version "17.0.16" 2025-07-15 LTS от [Bellsoft](https://github.com/bell-sw/Liberica/releases)
- IDE: IntelliJ IDEA Community Edition 2025.2
- [FasterXML/jackson](https://github.com/FasterXML/jackson)
- [pdfbox](https://github.com/apache/pdfbox)

## Проделанная работа

Созданы два класса, отражающие структуру
выписки: [Vypiska.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/Vypiska.java), [Section.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/Section.java).
Благодаря этим классам можно поделить выписку на разделы, а внутри каждого из разделов могут быть свои разделы.
Для вывода выписки был создан метод `public String getTree()`, который выводит содержимое выписки в таком формате:

```
ВЫПИСКА
  Секция 1
    пример строки таблицы 1
    пример строки таблицы 2
    пример строки таблицы 3
  Секция 2
    Подсекция 1
      пример строки таблицы 1
      пример строки таблицы 2
      пример строки таблицы 3
      Подсекция подсекции
        Под-под-под секция
    Подсекция 2
  Секция 3
```

- Конвертация из PDF в экземпляр выписки была вынесена в отдельный
  класс [ConverterOperations.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/ConverterOperations.java),
  реализующий
  интерфейс [Convertible.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/Convertible.java),
  который является полем в классе выписки.

### Реализация интерфейса [Convertible.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/Convertible.java):

- Метод `textToCollect()` позволяет постранично перевести весь текст выписки в коллекцию строк выписки, где игнорируются
  последние три строки (`DROPPED_END_LINES`) на каждой странице.
    - Три строки нужно убрать, так как на каждой странице есть колонтитул:
    ```
   Страница 1 из 
   Выписка из ЕГРЮЛ
   09.09.2025 21:02 ОГРН 1000000000000 17
    ```

- Метод `getTemplate()` позволяет считать шаблон
  выписки [resources/table_template.csv](https://github.com/MaxDSC/egryul_converter_pdf/tree/radical/src/main/resources/table_template.csv)
  в коллекцию.
    - Шаблон выписки представляет собой .csv-файл, где вначале буква `Р` - наименование раздела, `П` - наименование
      подраздела, `П#` - означает, что дальше идут перечисляемые подразделы, `П-` - означает, что подраздел
      пропускается.