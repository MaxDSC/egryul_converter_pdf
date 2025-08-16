package com.maxdsc;

import java.io.IOException;
import java.util.ArrayDeque;

public interface Convertible {
    /**
     * Получить весь текст PDF-выписки.
     *
     * @return строку, в которой будет весь текст PDF-выписки.
     */
    String getText() throws IOException;

    /**
     * Помещает весь текст выписки в коллекцию постранично, но до {@link Converter} DROPPED_END_LINES строк с конца,
     * для того, чтобы убрать со страницы нижний колонтитул типа: <div><pre>
     * Выписка из ЕГРЮЛ
     * 09.11.2025 21:52             ОГРН 1000000000000              Страница 1 из 17
     * </pre></div>
     *
     * @return коллекцию, где каждый элемент это строка из выписки.
     */
    ArrayDeque<String> textToCollect() throws IOException;
}
