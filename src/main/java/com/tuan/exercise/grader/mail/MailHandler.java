package com.tuan.exercise.grader.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Pattern;

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
    private Session mailSession;

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
        props.put("mail.smtp.host", Constant.Mail.SMTP_HOST);
        props.put("mail.smtp.port", Constant.Mail.SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // get session and connect to store
        mailSession = Session.getDefaultInstance(props);
        Store store = mailSession.getStore();
        store.connect(Constant.Mail.IMAP_HOST, this.username, this.password);

        return store;
    }

    public void downloadInboxZips(Store store, String subjectRegex, String destDirName) throws MessagingException {
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
            Log.info("Downloading from", from);
            int downCount = downloadAttachments(msg, from, Constant.IO.ZIP_EXT, destDirName);
            Log.info("Found", String.valueOf(downCount), "zip file(s)");
            if (downCount == 0) {
                sendReply(mailSession, msg, "You haven't attach any zip file, yet!");
            }
        }

        inbox.close(false);
    }

    private int downloadAttachments(Message msg, String from, String ext, String destDirName) {
        int downCount = 0;
        try {
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
                    downCount++;
                }
            }
            
        } catch (IOException | MessagingException e) {
            Log.err(e);
        }
        return downCount;
    }

    public void grade(String studentStorageDirName) {
        File baseDir = new File(studentStorageDirName);
        String[] studentDirNames = baseDir.list((file, fileName) -> file.isDirectory());

        for (String studentAddr : studentDirNames) {
            String classpath = new StringBuilder()
                    .append(studentStorageDirName).append(File.separator)
                    .append(studentAddr).toString();

            String answerFilePath = new StringBuilder()
                    .append(classpath).append(File.separator)
                    .append("test_pkg").append(File.separator)
                    .append("DemoApp.java").toString();

            Log.info("Marking " + studentAddr);
            float grade = AnswerUtil.getGrade(answerFilePath, classpath, Constant.Grader.APP_NAME);
            sendResult(studentAddr, grade);
        }
    }

    private void sendResult(String studentAddr, float grade) {
        Log.info("Sending grade to " + studentAddr);
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
            Log.info("Fnished sending grade to " + studentAddr);

        } catch (MessagingException e) {
            Log.err(e);
        }
    }
    
    private void sendReply(Session session, Message baseMessage, String content) {
        try {
            Log.info("Replying to", ((InternetAddress) baseMessage.getReplyTo()[0]).getAddress());
            Message repMessage = baseMessage.reply(false);
            repMessage.setFrom(new InternetAddress(this.username));
            repMessage.setText(content);
            repMessage.setReplyTo(baseMessage.getReplyTo());
            
            Transport transport = session.getTransport("smtp");
            transport.connect(this.username, this.password);
            transport.sendMessage(repMessage, repMessage.getAllRecipients());
            Log.info("Finished replying");
            
        } catch (MessagingException e) {
            Log.err(e);
        }
    }
}
