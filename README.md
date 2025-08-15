# Экспериментальная версия исходного кода: Конвертер PDF выписки из выписки ЕГРЮЛ/ЕГРИП

### Исходный должен позволять извлекать данные из PDF выписки из ЕГРЮЛ/ЕГРИП и поместить их в JSON, такого формата, который используется для официального [API ФНС](https://api-fns.ru/api_help#section_dannye)

## Используемое ПО и библиотеки

- Java версия: openjdk version "17.0.16" 2025-07-15 LTS от [Bellsoft](https://github.com/bell-sw/Liberica/releases)
- IDE: IntelliJ IDEA Community Edition 2025.2
- [FasterXML/jackson](https://github.com/FasterXML/jackson)
- [pdfbox](https://github.com/apache/pdfbox)

## Проделанная работа
#### На данный момент в JSON переводятся следующие поля:</h4>

<table class="table table-striped table-bordered table-condensed tree2" style="font-size: smaller;">
<thead><tr><th style="width: 190px;">Имя</th><th>Тип</th><th>Описание</th></tr></thead>
<tbody>
<tr class="treegrid-1"><td><i>Поля для <b>ЮЛ</b>:</i></td><td></td><td>(любое из полей может отсутствовать)</td></tr>
<tr class="treegrid-2 treegrid-parent-1"><td>ИНН</td><td>String</td><td>ИНН искомой компании</td></tr>
<tr class="treegrid-2 treegrid-parent-1"><td>КПП</td><td>String</td><td>КПП искомой компании</td></tr>
<tr class="treegrid-2 treegrid-parent-1"><td>ОГРН</td><td>String</td><td>ОГРН искомой компании</td></tr>
<tr class="treegrid-2 treegrid-parent-1"><td>НаимСокрЮЛ</td><td>String</td><td>Наименование ЮЛ краткое</td></tr>
<tr class="treegrid-2 treegrid-parent-1"><td>НаимПолнЮЛ</td><td>String</td><td>Наименование ЮЛ полное</td></tr>
<tr class="treegrid-2 treegrid-parent-1"><td>ДатаРег</td><td>String</td><td>Дата регистрации ЮЛ в формате YYYY-MM-DD</td></tr>
<tr class="treegrid-2 treegrid-parent-1"><td>Статус</td><td>String</td><td>Статус ЮЛ (ИП). Например, «Действующее», «Ликвидировано», «В состоянии реорганизации» и др.</td></tr>
</tbody>
</table>

#### Так будет выглядеть JSON:
```
{
  "ИНН": "2540096950",
  "КПП": "254001001",
  "ОГРН": "1032502271548",
  "ДатаОГРН": "15.12.2002"
  "ДатаРег": "26.03.1999",
  "Статус": null,
  "СпОбрЮЛ": "Создание юридического лица до 01.07.2002",
  "НаимСокрЮЛ": "ООО \"ПРИМЕР\"",
  "НаимПолнЮЛ": "ОБЩЕСТВО С ОГРАНИЧЕННОЙ ОТВЕТСТВЕННОСТЬЮ \"ПРИМЕР\"",
}
```
На данный момент поле "Статус" не заполняется, а поля с датами имеют формат DD.MM.YYYY".

