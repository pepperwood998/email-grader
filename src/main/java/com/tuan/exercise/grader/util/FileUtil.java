package com.tuan.exercise.grader.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    private FileUtil() {
    }

    public static void extractAll(String srcBaseDir, String destBaseDir) {
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

    private static void extractSingle(File compFile, String destBaseDir) {
        // test if file is compressed type
        try (DataInputStream zipTestIn = new DataInputStream(new FileInputStream(compFile))) {
            if (zipTestIn.readInt() != Constant.IO.ZIP_MAGIC)
                return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        String compFileFullname = compFile.getName();
        String destDirPath = new StringBuilder()
                .append(destBaseDir).append(File.separator)
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
                        .append(destDirPath).append(File.separator)
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
}
