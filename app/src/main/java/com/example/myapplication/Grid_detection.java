package com.example.myapplication;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

import java.util.ArrayList;
import java.util.List;

public class Grid_detection {
    protected static Bitmap detect(Bitmap captureImage, ImageView imageView){
        Bitmap bmp32 = captureImage.copy(Bitmap.Config.ARGB_8888, true);
        Mat capture_img = new Mat (bmp32.getWidth(),
                bmp32.getHeight(),
                CvType.CV_8UC1);
        Utils.bitmapToMat(bmp32, capture_img);

        //converting to grayscale image
        Mat img = new Mat(capture_img.rows(),capture_img.cols(),capture_img.type());
        Imgproc.cvtColor(capture_img, img, Imgproc.COLOR_RGB2GRAY);

        //Gausian blurring
        Imgproc.GaussianBlur(img, img,new Size(11,11), 0);

        //Adaptive Thresholding
        Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY, 5, 2);
        Core.bitwise_not(img, img);

        Imgproc.dilate(img, img , Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));

        // Imgproc.Canny(img, img, 80, 100);


        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(img, contours, new Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);

        double maxArea = -1;
        int maxAreaIdx = -1;
        MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint largest_contour = contours.get(0);

        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();

        for (int idx = 0; idx < contours.size(); idx++) {
            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize*0.05, true);
                if (approxCurve_temp.total() == 4) {
                    maxArea = contourarea;
                    maxAreaIdx = idx;
                    approxCurve=approxCurve_temp;
                    largest_contour = temp_contour;
                }
            }
        }

        Log.d("maxarea", String.valueOf(maxArea));
        double[] temp_double;
        Point[] points=new Point[4];
        temp_double = approxCurve.get(0,0);
        points[0]= new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(1,0);
        points[1] = new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(2,0);
        points[2]= new Point(temp_double[0], temp_double[1]);
        temp_double = approxCurve.get(3,0);
        points[3]= new Point(temp_double[0], temp_double[1]);





       /* MatOfPoint2f dst = new MatOfPoint2f(
                new Point(rectf.left, rectf.top),
                new Point(rectf.right,rectf.top),
                new Point(rectf.left,rectf.bottom),
                new Point(rectf.right,rectf.bottom)
        );*/
        org.opencv.core.Rect rect = Imgproc.boundingRect(contours.get(maxAreaIdx));
        Log.d("p11",String.valueOf(rect.x));
        Log.d("p12",String.valueOf(rect.y));
        Log.d("p13",String.valueOf(rect.width));
        Log.d("p14",String.valueOf(rect.height));

        Moments m=Imgproc.moments(contours.get(maxAreaIdx));
        Point centroid = new Point();
        centroid.x =m.get_m10() / m.get_m00();
        centroid.y = m.get_m01() / m.get_m00();
        Log.d("cen_x",String.valueOf(centroid.x));
        Log.d("cen_y",String.valueOf(centroid.y));
        MatOfPoint2f dst = new MatOfPoint2f(
                new Point(0,0),
                new Point(img.width(),0),
                new Point(img.width(),img.height()),
                new Point(0,img.height())
        );

        Point[] sortedPoints=new Point[4];
        for(int i=0;i<points.length;i++){
            if(points[i].x < centroid.x && points[i].y < centroid.y){
                sortedPoints[0]=points[i];
            }
            else if(points[i].x < centroid.x && points[i].y > centroid.y){
                sortedPoints[3]=points[i];
            }
            else if(points[i].x > centroid.x && points[i].y > centroid.y){
                sortedPoints[2]=points[i];
            }
            else{
                sortedPoints[1]=points[i];
            }
        }

        //MatOfPoint2f src = new MatOfPoint2f(points[0],points[1],points[2],points[3]);
        MatOfPoint2f src = new MatOfPoint2f(sortedPoints[0],sortedPoints[1],sortedPoints[2],sortedPoints[3]);
        Log.d("Area",String.valueOf(maxArea));
        Imgproc.cvtColor(img,img,Imgproc.COLOR_GRAY2RGB);
        Imgproc.drawContours(img, contours,maxAreaIdx, new Scalar(255,0,0),2);
        Log.d("p1",String.valueOf(points[0]));
        Log.d("p2",String.valueOf(points[1]));
        Log.d("p3",String.valueOf(points[2]));
        Log.d("p4",String.valueOf(points[3]));
        Mat warpMat = Imgproc.getPerspectiveTransform(src,dst);
        Mat destImage = new Mat();
        Imgproc.warpPerspective(img, destImage, warpMat, img.size());
        MatOfPoint2f src1 = new MatOfPoint2f(new Point(0,0),
                new Point(destImage.width()/9,0),
                new Point(destImage.width()/9,destImage.height()/9),
                new Point(0,destImage.height()/9)
        );

        MatOfPoint2f dst1 = new MatOfPoint2f(
                new Point(0,0),
                new Point(img.width(),0),
                new Point(img.width(),img.height()),
                new Point(0,img.height())
        );

        Mat warpMat1 = Imgproc.getPerspectiveTransform(src1,dst1);
        Mat destImage1 = new Mat();
        Imgproc.warpPerspective(destImage, destImage1, warpMat1, destImage.size());
        Imgproc.threshold(destImage1, destImage1, 128, 255, THRESH_BINARY);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(5, 5));
        Imgproc.dilate(destImage1, destImage1, kernel);




        Bitmap bmp=Bitmap.createBitmap(destImage1.width(),destImage1.height(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(destImage1, bmp);
        return bmp;





    }

}
