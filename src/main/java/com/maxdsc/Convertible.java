package com.maxdsc;

import java.io.IOException;

public interface Convertible {
    /**
     * Получить весь текст PDF-выписки.
     *
     * @return строку, в которой будет весь текст PDF-выписки.
     */
    String getText() throws IOException;
}
