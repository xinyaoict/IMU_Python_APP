package com.llui.idrink.Utils;

import android.content.Context;
import android.util.Log;

import androidx.camera.view.PreviewView;

public class PosManager {

    private static final int VIDEO_WIDTH = 1080;
    private static final int VIDEO_HEIGHT = 1440;

    public static int[] getPixelCoordinates(int posX, int posY, PreviewView previewView) {


        // Get PreviewView dimensions
        int previewViewWidth = previewView.getWidth();
        int previewViewHeight = previewView.getHeight();
        Log.i("ScreenPos", "PreviewView Size : X : " + previewViewWidth + " Y : " + previewViewHeight);


        // Assuming you have the aspect ratio of the camera preview set somewhere
        float aspectRatio = (float) 4 /3; // Replace with the actual aspect ratio of the camera preview

        // Calculate actual position on video
        float previewClickX, previewClickY;
        int previewWidth, previewHeight;
        if (previewViewWidth * aspectRatio <= previewViewHeight) {
            // Preview is wider than the previewView, fit horizontally
            previewWidth = previewViewWidth;
            previewHeight = (int) (previewViewWidth * aspectRatio);
            previewClickX = posX;
            float marginHeight = (previewViewHeight - previewHeight) / 2;
            previewClickY = posY -marginHeight;
        } else {
            previewHeight = previewViewHeight;
            previewWidth = (int) (previewViewHeight / aspectRatio);
            // Preview is taller than the previewView, fit vertically
            previewClickY = posY;
            float marginWidth = (previewViewWidth - previewWidth) / 2;
            Log.i("ScreenPos", "Margin : " + marginWidth);

            Log.i("ScreenPos", "PreviewWidth : " + previewWidth);


            previewClickX = posX - marginWidth;
        }

        int videoClickX = (int) previewClickX * VIDEO_WIDTH/previewWidth;
        int videoClickY =  (int) previewClickY *VIDEO_HEIGHT/previewHeight;

        Log.i("ScreenPos", "Preview : X : " + posX + " Y : " + posY);

        Log.i("ScreenPos", "Video : X : " + videoClickX + " Y : " + videoClickY);

        return new int[]{ videoClickX, videoClickY};
    }

    public static boolean checkVideoCoordinate(Context context,int videoClickX, int videoClickY) {
        if (videoClickX < 0 || videoClickX >= VIDEO_WIDTH || videoClickY < 0 || videoClickY >= VIDEO_HEIGHT) {
            DialogUtils.showOutofBoundDialog(context);
            return false;
        } else {
            return true;
        }

    }

}
