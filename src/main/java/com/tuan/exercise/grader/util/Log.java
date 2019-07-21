package com.tuan.exercise.grader.util;

import java.util.StringJoiner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(Log.class.getName());
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleLoggingFormatter());

        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
    }

    private Log() {
    }

    public static void info(String msg) {
        logger.log(Level.INFO, msg);
    }

    public static void err(Exception e) {
        logger.log(Level.SEVERE, e.getMessage());
    }
    
    public static void info(String... messages) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String msg : messages) {
            joiner.add(msg);
        }
        String msg = joiner.toString();
        logger.log(Level.INFO, msg);
    }

    private static class SimpleLoggingFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(record.getMessage()).append("\n");

            return sb.toString();
        }
    }
}
