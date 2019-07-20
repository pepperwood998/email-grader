package com.tuan.exercise.grader.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {

    private static final Logger LOG;

    static {
        LOG = Logger.getLogger(Log.class.getName());
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleLoggingFormatter());

        LOG.addHandler(handler);
        LOG.setLevel(Level.ALL);
        LOG.setUseParentHandlers(false);
    }

    private Log() {
    }

    public static void info(String msg) {
        LOG.log(Level.INFO, msg);
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
