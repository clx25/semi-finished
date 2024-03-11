package com.semifinished.auth.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaCreator {

    private final static char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E',
            'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};


    public static String create(int size) {

        StringBuilder sb = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < size; i++) {
            // 取随机字符索引
            int n = random.nextInt(chars.length);
            // 得到字符文本
            String code = String.valueOf(chars[n]);
            sb.append(code);
        }
        return sb.toString();

    }

    /**
     * 创建指定字符串的验证码图片
     *
     * @param code 指定的字符串
     * @return 验证码图片的base64编码
     * @throws IOException IOException
     */
    public static String createImage(String code) throws IOException {

        char[] chars = code.toCharArray();
        // 默认字符数量
        int size = code.length();
        // 默认干扰线数量
        int lines = 5;
        // 默认宽度
        int width = 120;
        // 默认高度
        int height = 35;
        // 默认字体大小
        int fontSize = 25;

        Color backgroundColor = Color.WHITE;

        // 创建空白图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图片画笔
        Graphics2D graphic = image.createGraphics();
        // 设置抗锯齿
        graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 设置画笔颜色
        graphic.setColor(backgroundColor);
        // 绘制矩形背景
        graphic.fillRect(0, 0, width, height);

        Random random = new Random();

        //graphic.setBackground(Color.WHITE);

        // 计算每个字符占的宽度，这里预留一个字符的位置用于左右边距
        int codeWidth = width / (size + 1);
        // 字符所处的y轴的坐标
        int y = height * 3 / 4;


        for (int i = 0; i < chars.length; i++) {
            // 设置随机颜色
            graphic.setColor(getRandomColor());
            // 初始化字体
            Font font = new Font(null, Font.BOLD + Font.ITALIC, fontSize);

            // 随机一个倾斜的角度 -45到45度之间
            int theta = random.nextInt(45);
            // 随机一个倾斜方向 左或者右
            theta = (random.nextBoolean()) ? theta : -theta;
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(Math.toRadians(theta), 0, 0);
            font = font.deriveFont(affineTransform);
            // 设置字体大小
            graphic.setFont(font);
            // 计算当前字符绘制的X轴坐标
            int x = (i * codeWidth) + (codeWidth / 2);

            // 画字符
            graphic.drawString(String.valueOf(chars[i]), x, y);

        }
        // 画干扰线
        for (int i = 0; i < lines; i++) {
            // 设置随机颜色
            graphic.setColor(getRandomColor());
            // 随机画线
            graphic.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);

        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }


    private static Color getRandomColor() {
        Random ran = new Random();
        return new Color(ran.nextInt(256), ran.nextInt(256), ran.nextInt(256));
    }
}
