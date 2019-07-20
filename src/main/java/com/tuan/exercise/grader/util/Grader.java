package com.tuan.exercise.grader.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Grader {

    public static void main(String[] args) throws IOException {
        int testCaseMonth = 2;
        int testCaseyear = 2020;
        Runtime.getRuntime().exec("javac ./src/main/java/com/tuan/exercise/answer/DemoApp.java");
        Process run = Runtime.getRuntime().exec(String.format(
                "java -cp ./src/main/java com.tuan.exercise.answer.DemoApp %d %d", testCaseMonth, testCaseyear));

        new Thread(() -> {
            BufferedReader input = new BufferedReader(new InputStreamReader(run.getInputStream()));
            String line = null;

            try {
                line = input.readLine();
                if (AnswerUtil.getDays(testCaseMonth, testCaseyear) == Integer.valueOf(line))
                    System.out.println("Correct");
                else
                    System.out.println("!!! Wrong !!!");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
