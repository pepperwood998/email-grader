package com.tuan.exercise.grader.mail;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.tuan.exercise.grader.util.AnswerUtil;
import com.tuan.exercise.grader.util.Constant;
import com.tuan.exercise.grader.util.Log;

public class MailHandler {

    private final String username;
    private final String password;

    public MailHandler(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Store connect() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", Constant.Mail.IMAP_STORE_TYPE);
        props.put("mail.imap.host", Constant.Mail.IMAP_HOST);
        props.put("mail.imap.port", Constant.Mail.IMAP_PORT);
        props.put("mail.imap.starttls.enable", "true");

        // get session and connect to store
        Session mailSession = Session.getDefaultInstance(props);
        Store store = mailSession.getStore();
        store.connect(Constant.Mail.IMAP_HOST, this.username, this.password);

        return store;
    }

    public void downloadInboxZips(Store store, String subjectRegex, String destDirName)
            throws IOException, MessagingException {
        File destDir = new File(destDirName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        Folder inbox = store.getFolder(Constant.Mail.FOLDER_INBOX);
        inbox.open(Folder.READ_ONLY);

        Message[] msgArr = inbox.getMessages();
        for (Message msg : msgArr) {
            String subject = msg.getSubject();
            Address[] froms = msg.getFrom();

            if (!Pattern.matches(subjectRegex, subject) || froms == null) {
                continue;
            }

            String from = ((InternetAddress) froms[0]).getAddress();
            this.downloadAttachments(msg, from, Constant.IO.ZIP_EXT, destDirName);
        }

        inbox.close(false);
    }

    private void downloadAttachments(Message msg, String from, String ext, String destDirName)
            throws IOException, MessagingException {

        Multipart mp = (Multipart) msg.getContent();
        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart bodyPart = mp.getBodyPart(i);

            // dealing with attachments only
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && bodyPart.getFileName() != null) {
                String fileFullname = bodyPart.getFileName();
                int dotInd = fileFullname.lastIndexOf('.');
                if (dotInd < 0 || !ext.equals(fileFullname.substring(dotInd)))
                    continue;

                String savedFileName = String.format("%s-%s", from, fileFullname);
                File download = new File(String.format("%s%s%s", destDirName, File.separator, savedFileName));
                InputStream netIn = bodyPart.getInputStream();
                try (OutputStream fileOut = new FileOutputStream(download)) {
                    byte[] buf = new byte[Constant.IO.NET_BUF];
                    int bytesRead;
                    while ((bytesRead = netIn.read(buf)) > 0) {
                        fileOut.write(buf, 0, bytesRead);
                    }
                }
            }
        }
    }

    public void extractAll(String srcBaseDir, String destBaseDir) {
        File srcDir = new File(srcBaseDir);
        if (!srcDir.exists())
            return;

        File destDir = new File(destBaseDir);
        if (!destDir.exists())
            destDir.mkdirs();

        // iterate through all files in the source directory
        File[] compressedFiles = srcDir.listFiles();
        for (File compFile : compressedFiles) {
            extractSingle(compFile, destBaseDir);
        }
    }

    private void extractSingle(File compFile, String destBaseDir) {
        // test if file is compressed type
        try (DataInputStream zipTestIn = new DataInputStream(new FileInputStream(compFile))) {
            if (zipTestIn.readInt() != Constant.IO.ZIP_MAGIC)
                return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        String compFileFullname = compFile.getName();
        String destDirPath = new StringBuilder()
                .append(destBaseDir)
                .append(File.separator)
                .append(compFileFullname.substring(0, compFileFullname.indexOf('-')))
                .toString();

        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (InputStream zipIn = new FileInputStream(compFile)) {
            ZipInputStream zis = new ZipInputStream(zipIn);
            ZipEntry zipEntry;
            byte[] buf = new byte[Constant.IO.FILE_BUF];
            while ((zipEntry = zis.getNextEntry()) != null) {
                File extracted = new File(new StringBuilder()
                        .append(destDirPath)
                        .append(File.separator)
                        .append(zipEntry.getName()).toString());
                if (zipEntry.isDirectory()) {
                    if (!extracted.exists())
                        extracted.mkdirs();
                } else {
                    try (FileOutputStream fos = new FileOutputStream(extracted)) {
                        int length;
                        while ((length = zis.read(buf)) > 0) {
                            fos.write(buf, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void grade() {
        String baseDirName = "./output/extracted";
        String appName = "test_pkg.DemoApp";
        File baseDir = new File(baseDirName);
        String[] studentDirNames = baseDir.list((file, fileName) -> file.isDirectory());

        for (String studentAddr : studentDirNames) {
            String classpath = new StringBuilder()
                    .append(baseDirName)
                    .append(File.separator)
                    .append(studentAddr)
                    .toString();
            String answerFilePath = new StringBuilder()
                    .append(classpath).append(File.separator)
                    .append("test_pkg").append(File.separator)
                    .append("DemoApp.java").toString();

            Log.info("Marking " + studentAddr);
            float grade = AnswerUtil.getGrade(answerFilePath, classpath, appName);
            Log.info("Sending grade to " + studentAddr);
            sendResult(studentAddr, grade);
            Log.info("Fnished sending grade to " + studentAddr);
        }
    }

    private void sendResult(String studentAddr, float grade) {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", Constant.Mail.SMTP_HOST);
        props.put("mail.smtp.port", Constant.Mail.SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(studentAddr));
            message.setSubject("Final Exam Grade");
            message.setText(new StringBuilder().append("Your grade: ").append(grade).toString());

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
