package com.tuan.exercise.grader;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.Store;

import com.tuan.exercise.grader.mail.MailHandler;

public class App {

    private static final String USERNAME = "sandboxman000@gmail.com";
    private static final String TEST_PASS = "1-2-3-4-5-6-7-8";

    public static void main(String[] args) {

        String subjectRegex = "^ITLAB-HOMEWORK.*$";
        String srcBaseDir = "./output/raw";
        String destBaseDir = "./output/extracted";
        MailHandler mailHandler = new MailHandler(USERNAME, TEST_PASS);

        try {
            Store store = mailHandler.connect();

            mailHandler.downloadInboxZips(store, subjectRegex, srcBaseDir);
            mailHandler.extractAll(srcBaseDir, destBaseDir);
            mailHandler.grade();

            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
