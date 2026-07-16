package com.workbuddy.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * 二维码工具类（ZXing）
 * 生成：返回 Base64 格式的 PNG data URI。
 * 解析：接收图片输入流，返回二维码文本内容。
 */
public class QrcodeUtils {

    /** 生成二维码（返回 PNG 的 Base64 data URI） */
    public static String generateBase64(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            BitMatrix matrix = new QRCodeWriter()
                    .encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.encodeBase64String(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("二维码生成失败: " + e.getMessage(), e);
        }
    }

    /** 解析二维码图片，返回文本内容 */
    public static String parse(InputStream input) {
        try {
            BufferedImage image = ImageIO.read(input);
            if (image == null) {
                throw new RuntimeException("无法读取图片，请确认上传的是有效的图片文件");
            }
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            throw new RuntimeException("二维码解析失败: " + e.getMessage(), e);
        }
    }
}
