package com.tuan.exercise.grader;

import javax.mail.MessagingException;
import javax.mail.Store;

import com.tuan.exercise.grader.mail.MailHandler;
import com.tuan.exercise.grader.util.FileUtil;
import com.tuan.exercise.grader.util.Log;

public class App {

    private static final String USERNAME = "sandboxman000@gmail.com";
    private static final String TEST_PASS = "1-2-3-4-5-6-7-8";

    public static void main(String[] args) {

        String subjectRegex = "^ITLAB-HOMEWORK.*$";
        String srcBaseDirName = "./output/raw";
        String destBaseDirName = "./output/extracted";
        String studentStorageDirName = "./output/extracted";
        MailHandler mailHandler = new MailHandler(USERNAME, TEST_PASS);

        try {
            Store store = mailHandler.connect();

            long start = System.currentTimeMillis();
            mailHandler.downloadInboxZips(store, subjectRegex, srcBaseDirName);
            FileUtil.extractAll(srcBaseDirName, destBaseDirName);
            mailHandler.grade(studentStorageDirName);
            long end = System.currentTimeMillis();
            Log.info("Core Process Duration:", String.valueOf(end - start), "ms");

        } catch (MessagingException e) {
            Log.err(e);
        }
        mailHandler.waitForMailProc();
    }
}
