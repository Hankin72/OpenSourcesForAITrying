package com.example.myapplication;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.HandsResult;
import com.zte.gesturesdemo.MainActivity;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class HandUtils {
    private static final String TAG = Stuff.getTAG(HandUtils.class);
    private static final String FILE = Stuff.FILE;

    public static final int[][] CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4},
            {0, 5}, {5, 6}, {6, 7}, {7, 8},
            {0, 9}, {9, 10}, {10, 11}, {11, 12},
            {0, 13}, {13, 14}, {14, 15}, {15, 16},
            {0, 17}, {17, 18}, {18, 19}, {19, 20}
    };

    public static String getLandmarksDebugString(Mat mat, LandmarkProto.NormalizedLandmarkList landmarks) {
        int landmarkIndex = 0;
        String landmarksString = "";
        for (LandmarkProto.NormalizedLandmark finger : landmarks.getLandmarkList()) {
            double x_pos = finger.getX();
            double y_pos = finger.getY();
            double z_pos = finger.getZ();
            int cx = (int) (x_pos * mat.cols());
            int cy = (int) (y_pos * mat.rows());


            landmarksString +=
                    "\t\tLandmark[" + landmarkIndex + "]: (" + cx + ", " + cy + ", " + z_pos + ")\n";
            ++landmarkIndex;
        }


        Log.d(TAG, "getLandmarksDebugString: " + landmarksString);
        return landmarksString;
    }

    /**
     *
     * @param result  记录手腕处坐标
     * @param showPixelValues
     */
    public static void logWristLandmark(HandsResult result, boolean showPixelValues) {
        if (result.multiHandLandmarks().isEmpty()) {
            // result.multiHandLandmarks(): 二维
            // 手部关键点的标准化坐标。
            // 标准化坐标的值范围通常在 [0,1] 之间。这意味着，无论输入图像的实际尺寸如何，这些坐标都会被归一化到这个范围内
            return;
        }
        LandmarkProto.NormalizedLandmark wristLandmark =
                result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.WRIST);
        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
        if (showPixelValues) {
            int width = result.inputBitmap().getWidth();
            int height = result.inputBitmap().getHeight();

            Log.i(TAG, String.format(
                    "MediaPipe Hand wrist coordinates (pixel values): x=%f, y=%f",
                    wristLandmark.getX() * width, wristLandmark.getY() * height));

        } else {
            Log.i(TAG, String.format(
                    "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                    wristLandmark.getX(), wristLandmark.getY()));
        }
        if (result.multiHandWorldLandmarks().isEmpty()) {
            return;
        }

        // result.multiHandWorldLandmarks(): 手部关键点在真实世界中的位置的三维信息。
        //这个方法返回的是手部关键点的世界坐标。
        //世界坐标是一个三维坐标系统，它提供了关键点在真实世界中的位置信息。这些坐标通常是以米为单位的
        LandmarkProto.Landmark wristWorldLandmark =
                result.multiHandWorldLandmarks().get(0).getLandmarkList().get(HandLandmark.WRIST);

        Log.i(TAG, String.format(
                "MediaPipe Hand wrist world coordinates (in meters with the origin at the hand's"
                        + " approximate geometric center): x=%f m, y=%f m, z=%f m",
                wristWorldLandmark.getX(), wristWorldLandmark.getY(), wristWorldLandmark.getZ()));
    }

    public static String baseHandCount(List<LandmarkProto.NormalizedLandmark> handLandmarkList) {
//        从拇指开始是one，two这样
        List<Boolean> resultList = new ArrayList<>();
//        写字判断类别：
        /*
            计算拇指是否弯曲
            计算第四个点到第17点距离小于第5点到17点距离
         */
        float seventhreenx = handLandmarkList.get(17).getX();
        float seventhreeny = handLandmarkList.get(17).getY();
        float seventhreenz = handLandmarkList.get(17).getZ();
        float fourx = handLandmarkList.get(4).getX();
        float foury = handLandmarkList.get(4).getY();
        float fourz = handLandmarkList.get(4).getZ();
        float fivex = handLandmarkList.get(5).getX();
        float fivey = handLandmarkList.get(5).getY();
        float fivez = handLandmarkList.get(5).getZ();
        float distance4 =
                (fourx - seventhreenx) * (fourx - seventhreenx) +
                        (foury - seventhreeny) * (foury - seventhreeny) +
                        (fourz - seventhreenz) * (fourz - seventhreenz);
        float distance5 =
                (fivex - seventhreenx) * (fivex - seventhreenx) +
                        (fivey - seventhreeny) * (fivey - seventhreeny) +
                        (fivez - seventhreenz) * (fivez - seventhreenz);
        if (distance5 - distance4 > 0) {
//            有弯曲
            resultList.add(true);

        } else {
            resultList.add(false);
//            没有弯曲
        }
        /*
            计算四个手指是否弯曲
            手指的指尖距离大于关节距离
         */

        // 定义0点坐标zerox,zeroy,zeroz
        float zerox = handLandmarkList.get(0).getX();
        float zeroy = handLandmarkList.get(0).getY();
        float zeroz = handLandmarkList.get(0).getZ();

        //该遍历是:(6,8),(10,12),(14,16),(18,20)
        int[] intarr = new int[]{6, 8, 10, 12, 14, 16, 18, 20};

        for (int i = 0; i < 4; i++) {//0,1,2,3,,取i,i+1),2i,2i+1
            float closex = handLandmarkList.get(intarr[2 * i]).getX();
            float closey = handLandmarkList.get(intarr[2 * i]).getY();
            float closez = handLandmarkList.get(intarr[2 * i]).getZ();
            float farx = handLandmarkList.get(intarr[2 * i + 1]).getX();
            float fary = handLandmarkList.get(intarr[2 * i + 1]).getY();
            float farz = handLandmarkList.get(intarr[2 * i + 1]).getZ();
            //取出点坐标与0点求欧式距离
            float close_distance = (closex - zerox) * (closex - zerox) + (closey - zeroy) * (closey - zeroy) + (closez - zeroz) * (closez - zeroz);
            float far_distance = (farx - zerox) * (farx - zerox) +
                    (fary - zeroy) * (fary - zeroy) +
                    (farz - zeroz) * (farz - zeroz);
            float between = close_distance - far_distance;
            float zero = 0;
            if (between > zero) {
                //弯曲
                resultList.add(true);
            } else {
                //  没有弯曲
                resultList.add(false);

            }
        }

        String code = "";
        for (boolean b : resultList) {
            if (b) {
                code += "1";
            } else {
                code = code + "0";
            }
        }
        return code;
    }


    public static String twoHandDetect(List<LandmarkProto.NormalizedLandmark> landmark1, List<LandmarkProto.NormalizedLandmark> landmark2) {
        String code1 = baseHandCount(landmark1);
        String code2 = baseHandCount(landmark2);
        //判断Love
        if (code1.endsWith("111") && code2.endsWith("111") || code1.endsWith("000") && code2.endsWith("000")) {
            // 两个指尖距离小于指尖到关节的距离
            float hand1x4 = landmark1.get(4).getX();
            float hand1y4 = landmark1.get(4).getY();
            float hand1z4 = landmark1.get(4).getZ();

            float hand2x4 = landmark2.get(4).getX();
            float hand2y4 = landmark2.get(4).getY();
            float hand2z4 = landmark2.get(4).getZ();

            float hand1x8 = landmark1.get(8).getX();
            float hand1y8 = landmark1.get(8).getY();
            float hand1z8 = landmark1.get(8).getZ();

            float hand2x8 = landmark2.get(8).getX();
            float hand2y8 = landmark2.get(8).getY();
            float hand2z8 = landmark2.get(8).getZ();

            //距离计算:两只手食指指尖和拇指指尖距离计算
            float distance_muzhi =
                    (hand1x4 - hand2x4) * (hand1x4 - hand2x4) +
                            (hand1y4 - hand2y4) * (hand1y4 - hand2y4) +
                            (hand1z4 - hand2z4) * (hand1z4 - hand2z4);
            float distance_shizhi =
                    (hand1x8 - hand2x8) * (hand1x8 - hand2x8) +
                            (hand1y8 - hand2y8) * (hand1y8 - hand2y8) +
                            (hand1z8 - hand2z8) * (hand1z8 - hand2z8);
            //指尖到指尖关节距离计算
            float hand1x3 = landmark1.get(3).getX();
            float hand1y3 = landmark1.get(3).getY();
            float hand1z3 = landmark1.get(3).getZ();
            float distance1_34 =
                    (hand1x4 - hand1x3) * (hand1x4 - hand1x3) +
                            (hand1y4 - hand1y3) * (hand1y4 - hand1y3) +
                            (hand1z4 - hand1z3) * (hand1z4 - hand1z3);
            float hand1x7 = landmark1.get(7).getX();
            float hand1y7 = landmark1.get(7).getY();
            float hand1z7 = landmark1.get(7).getZ();
            float distance1_78 =
                    (hand1x8 - hand1x7) * (hand1x8 - hand1x7) +
                            (hand1y8 - hand1y7) * (hand1y8 - hand1y7) +
                            (hand1z8 - hand1z7) * (hand1z8 - hand1z7);
            float zero = 0;
            if ((distance1_34 * 1.5 - distance_muzhi > zero) && (distance1_78 - distance_shizhi > zero)) {
                //手势正确
                return "Love";
            }
        }
        //判断flower 五根手指都不弯曲
        if ("00000".equals(code1) && "00000".equals(code2)) {
            //0点之间的距离：
            float hand1x0 = landmark1.get(0).getX();
            float hand1y0 = landmark1.get(0).getY();
            float hand1z0 = landmark1.get(0).getZ();

            float hand2x0 = landmark2.get(0).getX();
            float hand2y0 = landmark2.get(0).getY();
            float hand2z0 = landmark2.get(0).getZ();
            float distance0_0 =
                    (hand1x0 - hand2x0) * (hand1x0 - hand2x0) +
                            (hand1y0 - hand2y0) * (hand1y0 - hand2y0) +
                            (hand1z0 - hand2z0) * (hand1z0 - hand2z0);
            float hand1x17 = landmark1.get(17).getX();
            float hand1y17 = landmark1.get(17).getY();
            float hand1z17 = landmark1.get(17).getZ();

            float distance0_17 =
                    (hand1x0 - hand1x17) * (hand1x0 - hand1x17) +
                            (hand1y0 - hand1y17) * (hand1y0 - hand1y17) +
                            (hand1z0 - hand1z17) * (hand1z0 - hand1z17);
            float zero = 0;
            if (distance0_17 - distance0_0 > zero) {
                return "flower";
            }
        }
        return "none";
    }

    public static String basePost(String code){
        String res="";
        switch (code){
            case "01111":
                res="GOOD";
//                img_love.setImageResource(R.drawable.good);
                break;
            case "10111":
                res="数字1";
//                img_love.setImageResource(R.drawable.one);
                break;
            case "11111":
                res="fist";
//                img_love.setImageResource(R.drawable.fist);
                break;


            //没有图片
            case "10011":
                res="数字2";
                break;
            case "10001":
                res="数字3";
                break;
            case "10000":
                res="数字4";
                break;
            case "00000":
                res="数字5";
                break;
            case "01110":
                res="数字6";
                break;
//            case "00111":
//                res="数字7";
//                break;
            case "00111":
                res="数字8";
                break;
            case "11011":
            case "01011":
                res="国际友好手势";
                break;
            case "11000":
                res="OK";
                break;
//            case "00110":
//                res="Love2";
//                break;
            default:
                res="none";
                break;
        }

        return res;

    }


}