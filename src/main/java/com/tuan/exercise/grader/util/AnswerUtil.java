package com.tuan.exercise.grader.util;

public class AnswerUtil {

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
}
