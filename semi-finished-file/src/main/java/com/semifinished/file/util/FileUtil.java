package com.semifinished.file.util;

import com.semifinished.core.exception.ProjectRuntimeException;
import com.semifinished.file.exception.FileUploadException;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 文件相关工具类
 */
public class FileUtil {

    private static String filePath;

    /**
     * 判断是否需要执行保存文件操作
     *
     * @param file 文件绝对距离，包含文件名
     * @return true需要保存，false不需要保存
     * @throws FileUploadException 没有该路径，并且创建失败
     */
    public static boolean fileExists(File file) throws FileUploadException {
        if (file.exists()) {
            return false;
        }
        File parentFile = file.getParentFile();
        if (parentFile.exists()) {
            return true;
        }
        if (!parentFile.mkdir()) {
            if (!parentFile.mkdirs()) {
                throw new FileUploadException(file.getAbsolutePath() + "创建失败");
            }
        }
        return true;
    }

    public static void saveFile(MultipartFile multipartFile, File file) throws FileUploadException, IOException {
        if (file.exists()) {
            return;
        }
        File parentFile = file.getParentFile();
        if (!(parentFile.exists() || parentFile.mkdir() || parentFile.mkdirs())) {
            throw new FileUploadException(file.getAbsolutePath() + "创建失败");
        }
        multipartFile.transferTo(file);
    }

    public static void randomAccessSaveFile(MultipartFile multipartFile, File file, long position) throws IOException, FileUploadException {
        if (!file.exists()) {
            saveFile(multipartFile, file);
            return;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            randomAccessFile.seek(position);
            randomAccessFile.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public static void existOrCrate(File file) throws FileUploadException {
        if (file.exists()) {
            return;
        }
        if (!file.mkdir()) {
            if (!file.mkdirs()) {
                throw new FileUploadException(file.getAbsolutePath() + "创建失败");
            }
        }
    }

    public static String getDefaultPath() {
        if (filePath != null) {
            return filePath;
        }
        try {
            //获取调用栈
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                //遍历调用栈，如果这个栈里面有main方法，就获取class
                if ("main".equals(stackTraceElement.getMethodName())) {
                    ApplicationHome applicationHome = new ApplicationHome(Class.forName(stackTraceElement.getClassName()));
                    File file = applicationHome.getSource();

                    filePath = file.getParentFile().toString() + File.separator + "upload";
                    return filePath;
                }
            }
        } catch (ClassNotFoundException ignored) {

        }
        throw new ProjectRuntimeException("无法推断main函数所在类");
    }

    public static String md5(byte[] bytes) {
        return digest(bytes, "MD5");
    }

    public static String sha1(byte[] bytes) {
        return digest(bytes, "SHA1");
    }

    private static String digest(byte[] bytes, String algorithm) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ignored) {
            throw new RuntimeException("算法错误");
        }
        byte[] digest = messageDigest.digest(bytes);
        //转换为16进制
        return new BigInteger(1, digest).toString(16);
    }
}
