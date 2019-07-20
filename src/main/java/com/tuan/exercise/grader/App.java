package com.tuan.exercise.grader;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Store;

import com.tuan.exercise.grader.mail.MailHandler;

public class App {

    private static final String USERNAME;
    private static final String TEST_PASS;
    private static final Logger LOG;

    static {
        USERNAME = "sandboxman000@gmail.com";
        TEST_PASS = "1-2-3-4-5-6-7-8";

        LOG = Logger.getLogger(App.class.getName());
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleLoggingFormatter());

        LOG.addHandler(handler);
        LOG.setLevel(Level.ALL);
        LOG.setUseParentHandlers(false);
    }

    public static void main(String[] args) {

        String subjectRegex = "^ITLAB-HOMEWORK.*$";
        String srcBaseDir = "./output/raw";
        String destBaseDir = "./output/extracted";
        MailHandler mailHandler = new MailHandler(USERNAME, TEST_PASS);

        try {
            Store store = mailHandler.connect();
            
            mailHandler.downloadInboxZips(store, subjectRegex, srcBaseDir);
            mailHandler.extractAll(srcBaseDir, destBaseDir);
            
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class SimpleLoggingFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(record.getMessage());
            sb.append("\n");
            return sb.toString();
        }
    }
}
