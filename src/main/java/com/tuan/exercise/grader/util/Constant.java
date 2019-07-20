package com.tuan.exercise.grader.util;

public class Constant {
    private Constant() {
    }

    public static class Mail {
        private Mail() {

        }

        public static final String FOLDER_INBOX = "INBOX";

        public static final String IMAP_HOST = "imap.gmail.com";
        public static final String IMAP_PORT = "993";
        public static final String IMAP_STORE_TYPE = "imaps";

        public static final String SMTP_HOST = "smtp.gmail.com";
        public static final String SMTP_PORT = "587";
    }

    public static class IO {
        private IO() {
        }

        public static final String ZIP_EXT = ".zip";
        public static final int ZIP_MAGIC = 0x504b0304;
        public static final int NET_BUF = 1024;
        public static final int FILE_BUF = 1024;
    }
}
