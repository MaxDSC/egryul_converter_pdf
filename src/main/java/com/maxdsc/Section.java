package com.maxdsc;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class Section {
    private String name;
    private ArrayDeque<String> rows;
    private ArrayDeque<Section> subsections;

    public Section() {
        this.rows = new ArrayDeque<>();
        this.subsections = new ArrayDeque<>();
    }

    public Section(String name) {
        this.rows = new ArrayDeque<>();
        this.subsections = new ArrayDeque<>();
        this.name = name;
    }

    /**
     * Получить строку дерева раздела.
     * Например, <div><pre>
     * Секция 2
     *   Подсекция 1
     *     пример строки таблицы 1
     *     пример строки таблицы 2
     *     пример строки таблицы 3
     *     Подсекция подсекции
     *       Под-под-под секция
     *   Подсекция 2
     * </pre></div>
     *
     * @param indent - отступ.
     * @return строку, представляющую раздел выписки с подразделами, внутри раздела и каждого из подразделов могут быть строки текста.
     */
    public String getTree(String indent){
        String tree = indent + name;
        if (isHaveSubsections()){
            indent = indent + "  ";
            for(Section sub : subsections){
                tree = tree + "\n" + getContent(indent) + sub.getTree(indent);
            }
        } else {
            tree = tree + "\n" + getContent(indent);
        }
        return tree;
    }

    /**
     * Получить строку, в которой будут все строки раздела.
     * @param indent - отступ.
     * @return строку, в которой будут все строки раздела.
     */
    private String getContent(String indent){
        String content = "";
        if (rows != null && !rows.isEmpty()){
            for(String s : rows){
                content = content + indent + s + "\n";
            }
        }
        return content;
    }

    /**
     * Добавить подраздел в текущий раздел.
     * @param sub подраздел.
     */
    public void addSubSection(Section sub){
        subsections.add(sub);
    }

    /**
     * Имеет ли раздел подразделы.
     * @return true, если имеет подразделы.
     */
    public boolean isHaveSubsections(){
        return !subsections.isEmpty();
    }

    public void addRows(String row){
        this.rows.add(row);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayDeque<String> getRows() {
        return rows;
    }

    public void setRows(ArrayDeque<String> rows) {
        this.rows = rows;
    }

}
