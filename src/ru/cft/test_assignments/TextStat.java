package ru.cft.test_assignments;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TextStat {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        if (args.length != 3) {
            System.out.println("Вы указали неправильное количество аргументов программы.");
            System.out.println("Нужно указать три аргумента:");
            System.out.println("1. Путь к файлу c исходным текстом, или просто имя файла, если файл расположен рядом с скриптом.");
            System.out.println("2. Путь к файлу c c шаблонами, или просто имя файла, если файл расположен рядом с скриптом.");
            System.out.println("3. Путь к файлу c для вывода результата, или просто имя файла, если файл нужно выложить рядом с скриптом.");
            return;
        }

        String sourceFileName = args[0];
        String templateFileName = args[1];
        String resultFileName = args[2];

        List<String> source = new ArrayList<>();
        try {
            for (String line : Files.lines(Paths.get(sourceFileName), StandardCharsets.UTF_8).collect(Collectors.toList())) {
                source.addAll(Arrays.asList(line.split(" ")));
            }
        } catch (IOException e) {
            printError("ОШИБКА. Проверьте путь к файлу источнику данных: " + sourceFileName, e);
            return;
        }

        List<String> templates;
        try {
            templates = Files.lines(Paths.get(templateFileName), StandardCharsets.UTF_8).collect(Collectors.toList());
        } catch (IOException e) {
            printError("ОШИБКА. Проверьте путь к файлу с шаблонами: " + templateFileName, e);
            return;
        }

        List<String> result = new ArrayList<>();
        for (String template : templates) {
                result.add(countMatches(source, template.trim()));
        }

        try {
            Files.write(Paths.get(resultFileName), result, StandardCharsets.UTF_8);
        } catch (IOException e) {
            printError("ОШИБКА. Проверьте путь к файлу для сохранения результата: " + resultFileName, e);
            return;
        }

        System.out.println("Готово за " + (System.currentTimeMillis() - startTime) + " мсек.");
    }

    /**
     * Вспомогательный метод для распределения подсчетов для двух типов шаблонов: последовательности и шаблоны в формате "буква - количество вхождений"
     *
     * @param source   - исходные данные: список слов
     * @param template - искомый шаблон
     * @return строка в формате: шаблон количество совпадений
     */
    private static String countMatches(List<String> source, String template) {
        if (template.startsWith("\"")) {
            return countSequenceMatches(source, template);
        } else {
            return countPatternMatches(source, template);
        }
    }

    /**
     * Проверяет сколько раз искомая последовательность встречается в списке слов.
     *
     * @param source   - исходные данные: список слов
     * @param sequence - искомая последовательность.
     * @return строка в формате: шаблон количество совпадений
     */
    private static String countSequenceMatches(List<String> source, String sequence) {
        if (!sequence.startsWith("\"") || !sequence.endsWith("\"")) {
            System.out.println("ОШИБКА. Последовательность должна быть заключена в кавычки с двух сторон. Проверьте последовательность: " + sequence);
            return String.format("%-10s %s", sequence, -1);
        }

        int count = 0;
        for (String word : source) {
            if (word.contains(sequence.substring(1, sequence.length() - 1))) {
                count++;
            }
        }
        return String.format("%-10s %s", sequence, count);
    }

    /**
     * Проверяет сколько раз шаблон содержится в списке слов.
     *
     * @param source  - исходные данные: список слов
     * @param pattern - шаблон.
     * @return строка в формате: шаблон количество совпадений
     */
    private static String countPatternMatches(List<String> source, String pattern) {
        char[] chars = pattern.toCharArray();
        if (chars.length % 2 != 0) {
            System.out.println("ОШИБКА. Шаблон должен состоять из четного набора символов. Проверьте шаблон: " + pattern);
            return String.format("%-10s %s", pattern, -1);
        }
        for (int i = 1; i < chars.length; i += 2) {
            if (!Character.isDigit(chars[i])) {
                System.out.println("ОШИБКА. Каждый четный символ шаблона должен быть цифрой. Проверьте шаблон: " + pattern);
                return String.format("%-10s %s", pattern, -1);
            }
        }

        int count = 0;
        for (String word : source) {
            boolean needCount = false;
            for (int i = 0; i < chars.length; ) {
                if (isMatchesWord(word, chars[i], chars[i + 1] - '0')) {
                    i += 2;
                    needCount = true;
                } else {
                    needCount = false;
                    break;
                }
            }
            if (needCount) {
                count++;
            }
        }
        return String.format("%-10s %s", pattern, count);
    }

    /**
     * Проверяет, что в слове встречается искомый символ необходимое количество раз.
     *
     * @param word        - слово, в котором осуществляется поиск.
     * @param wantedChar  - разыскиваемый символ.
     * @param neededCount - необходимое количество совпадений искомого символа в слове.
     * @return true - если в слове содержится искомый символ необходимое количество раз или более.
     * false - если искомый символ не содержится в слове необходимое количество раз.
     */
    private static boolean isMatchesWord(String word, char wantedChar, int neededCount) {
        int count = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == wantedChar) {
                count++;
                if (neededCount == count) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Выводит информацию об ошибке в консоль.
     *
     * @param message - сообщение
     * @param e       - ошибка.
     */
    private static void printError(String message, Exception e) {
        System.out.println(message);
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
}
