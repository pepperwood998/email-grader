package com.tuan.exercise.grader.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AnswerUtil {
    
    private static final Map<Integer[], Integer> TEST_CASES = new HashMap<>();
    
    static {
        TEST_CASES.put(new Integer[] { 1, 2019 }, 31);
        TEST_CASES.put(new Integer[] { 4, 2019 }, 30);
        TEST_CASES.put(new Integer[] { 7, 2019 }, 31);
        TEST_CASES.put(new Integer[] { 8, 2019 }, 31);
        TEST_CASES.put(new Integer[] { 11, 2019 }, 30);
        TEST_CASES.put(new Integer[] { 12, 2019 }, 31);
        TEST_CASES.put(new Integer[] { 2, 2019 }, 28);
        TEST_CASES.put(new Integer[] { 2, 2020 }, 29);
    }

    private AnswerUtil() {

    }

    public static int getDays(int month, int year) {
        if (month == 2) {
            if (year % 4 == 0)
                return 29;
            else
                return 28;
        }

        if (month % 2 == 0) {
            if (month < 8)
                return 30;
            else
                return 31;
        } else {
            if (month < 8)
                return 31;
            else
                return 30;
        }
    }
    
    public static float getGrade(String answerFilePath, String classpath, String appName) {

        float testCaseNum = TEST_CASES.size();
        float passed = 0;

        try {
            Process compileProc = Runtime.getRuntime().exec(String.format("javac %s", answerFilePath));
            String tempRunCmd = String.join(" ", "java", "-cp", classpath, appName);
            for (Map.Entry<Integer[], Integer> entry : TEST_CASES.entrySet()) {
                int compileStat = compileProc.waitFor();
                if (compileStat == 0) {

                    StringBuilder runCmd = new StringBuilder(tempRunCmd);
                    for (Integer input : entry.getKey()) {
                        runCmd.append(" ").append(input);
                    }
                    Process runProc = Runtime.getRuntime().exec(runCmd.toString());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(runProc.getInputStream()));
                    String answer = reader.readLine();
                    if (Integer.valueOf(answer).equals(entry.getValue())) {
                        passed++;
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (passed / testCaseNum) * 10.0f;
    }
}
