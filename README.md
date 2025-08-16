# Экспериментальная версия исходного кода: Конвертер PDF выписки из выписки ЕГРЮЛ/ЕГРИП

### Исходный должен позволять извлекать данные из PDF выписки из ЕГРЮЛ/ЕГРИП и поместить их в JSON, такого формата, который используется для официального [API ФНС](https://api-fns.ru/api_help#section_dannye)

## Используемое ПО и библиотеки

- Java версия: openjdk version "17.0.16" 2025-07-15 LTS от [Bellsoft](https://github.com/bell-sw/Liberica/releases)
- IDE: IntelliJ IDEA Community Edition 2025.2
- [FasterXML/jackson](https://github.com/FasterXML/jackson)
- [pdfbox](https://github.com/apache/pdfbox)

## Проделанная работа
Созданы два класса, отражающие структуру выписки: [Vypiska.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/Vypiska.java), [Section.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/Section.java).
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
Конвертация из PDF в экземпляр выписки была вынесена в отдельный класс [ConverterOperations.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/ConverterOperations.java), реализующий интерфейс [Convertible.java](https://github.com/MaxDSC/egryul_converter_pdf/blob/radical/src/main/java/com/maxdsc/Convertible.java), который является полем в классе выписки.