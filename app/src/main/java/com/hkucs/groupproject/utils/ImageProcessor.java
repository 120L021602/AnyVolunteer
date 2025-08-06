package com.hkucs.groupproject.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {

    // 定义打码类型
    public static class MaskType {
        public static final String SOLID = "solid";
        public static final String TRANSLUCENT = "translucent";
        public static final String PIXELATE = "pixelate";
        public static final String COLOR_JITTER = "color_jitter";
        public static final String REPLACE_FACE = "replace_face";
    }

    // 参数组合类
    public static class ObfuscationParams {
        public float coverageRatio; // 覆盖比例
        public String maskType; // 打码类型
        public int granularity; // 颗粒度

        public ObfuscationParams(float coverageRatio, String maskType, int granularity) {
            this.coverageRatio = coverageRatio;
            this.maskType = maskType;
            this.granularity = granularity;
        }
    }

    // 加载分类器
    private static CascadeClassifier loadCascade(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            File cascadeFile = new File(context.getFilesDir(), fileName);
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            return new CascadeClassifier(cascadeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 人脸检测方法
    public static List<Rect> detectFaces(Bitmap bitmap, Context context) {
        List<Rect> faceRects = new ArrayList<>();

        // 将 Bitmap 转换为 OpenCV 的 Mat 格式
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY); // 转为灰度图

        // 加载分类器
        CascadeClassifier faceDetector = loadCascade(context, "haarcascade_frontalface_default.xml");
        if (faceDetector == null || faceDetector.empty()) {
            Log.e("FaceDetector", "Failed to load face detector!");
            throw new RuntimeException("无法加载分类器！");
        }
        else {
            Log.d("FaceDetector", "Face detector loaded successfully.");
        }

        // 检测人脸
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(mat, faces);

        // 将检测到的人脸区域转换为 Rect 列表
        for (org.opencv.core.Rect face : faces.toArray()) {
            faceRects.add(new Rect(face.x, face.y, face.x+face.width, face.y+face.height));
        }

        return faceRects;
    }

    // 应用模糊处理
    public static Bitmap applyFaceObfuscation(Bitmap image, List<Rect> faceBoxes, ObfuscationParams params, Bitmap replaceFace) {
        Bitmap result = image.copy(image.getConfig(), true);

        for (Rect faceBox : faceBoxes) {
            switch (params.maskType) {
                case MaskType.SOLID:
                    result = applySolidMask(result, faceBox, params.coverageRatio);
                    break;
                case MaskType.TRANSLUCENT:
                    result = applyTranslucentMask(result, faceBox, params.coverageRatio, params.granularity);
                    break;
                case MaskType.PIXELATE:
                    result = applyPixelateMask(result, faceBox, params.coverageRatio, params.granularity);
                    break;
                case MaskType.COLOR_JITTER:
                    result = applyColorJitter(result, faceBox, params.coverageRatio);
                    break;
                case MaskType.REPLACE_FACE:
                    result = applyReplaceFace(result, faceBox, replaceFace);
                    break;
            }
        }

        return result;
    }

    // 实心模糊
    private static Bitmap applySolidMask(Bitmap image, Rect faceBox, float coverageRatio) {
        Rect coveredArea = calculateCoveredArea(faceBox, coverageRatio);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(coveredArea, paint);
        return image;
    }

    // 半透明模糊
    private static Bitmap applyTranslucentMask(Bitmap image, Rect faceBox, float coverageRatio, int granularity) {
        Rect coveredArea = calculateCoveredArea(faceBox, coverageRatio);
        Bitmap blurred = Bitmap.createBitmap(image, coveredArea.left, coveredArea.top, coveredArea.width(), coveredArea.height());
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setAlpha(128); // 半透明
        canvas.drawBitmap(blurred, coveredArea.left, coveredArea.top, paint);
        return image;
    }

    // 像素化处理
    private static Bitmap applyPixelateMask(Bitmap image, Rect faceBox, float coverageRatio, int granularity) {
        Rect coveredArea = calculateCoveredArea(faceBox, coverageRatio);
        Bitmap faceRegion = Bitmap.createBitmap(image, coveredArea.left, coveredArea.top, coveredArea.width(), coveredArea.height());
        Bitmap scaledDown = Bitmap.createScaledBitmap(faceRegion, coveredArea.width() / granularity, coveredArea.height() / granularity, false);
        Bitmap pixelated = Bitmap.createScaledBitmap(scaledDown, coveredArea.width(), coveredArea.height(), false);
        Canvas canvas = new Canvas(image);
        canvas.drawBitmap(pixelated, coveredArea.left, coveredArea.top, null);
        return image;
    }

    // 颜色抖动
    private static Bitmap applyColorJitter(Bitmap image, Rect faceBox, float coverageRatio) {
        Rect coveredArea = calculateCoveredArea(faceBox, coverageRatio);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(Color.RED); // 示例：颜色调整为红色
        canvas.drawRect(coveredArea, paint);
        return image;
    }

    // 人脸替换
    private static Bitmap applyReplaceFace(Bitmap image, Rect faceBox, Bitmap replaceFace) {
        if (replaceFace == null) return image;
        Bitmap resizedFace = Bitmap.createScaledBitmap(replaceFace, faceBox.width(), faceBox.height(), false);
        Canvas canvas = new Canvas(image);
        canvas.drawBitmap(resizedFace, faceBox.left, faceBox.top, null);
        return image;
    }

    // 计算覆盖区域
    private static Rect calculateCoveredArea(Rect faceBox, float coverageRatio) {
        int width = (int) (faceBox.width() * coverageRatio);
        int height = (int) (faceBox.height() * coverageRatio);
        int left = faceBox.centerX() - width / 2;
        int top = faceBox.centerY() - height / 2;
        return new Rect(left, top, left + width, top + height);
    }
}