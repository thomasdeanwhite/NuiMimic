package com.sheffield.leapmotion;

import com.sheffield.leapmotion.instrumentation.MockSystem;

/**
 * Created by thomas on 18/11/2016.
 */
public class ProgressBar {

    interface BarDrawer {
        String drawBar(int width, float percent);
        String getBarHeader(int width);
    }

    private static BarDrawer barDrawer = new HeaderedBarDrawer();

    public static String getProgressBar(int bars, float percent){
        if (bars%2 == 0){
            bars--;
        }
        return barDrawer.drawBar(bars, percent);
    }

    public static String getHeaderBar(int bars){
        if (bars%2 == 0){
            bars--;
        }
        return barDrawer.getBarHeader(bars);
    }

    static class PlainBarDrawer implements BarDrawer {

        @Override
        public String drawBar(int width, float percent) {
            String progress = "[";
            int b1 = (int) (percent * width);
            for (int i = 0; i < b1; i++) {
                progress += "-";
            }
            //progress += ">";
            int b2 = width - b1;
            for (int i = 0; i < b2; i++) {
                progress += " ";
            }
            progress += "] " + ((int) (percent * 1000)) / 10f + "%";

            return progress;
        }

        @Override
        public String getBarHeader(int width) {
            return "";
        }
    }

    static class HeaderedBarDrawer implements BarDrawer {

        @Override
        public String drawBar(int width, float percent) {
            String progChar = "=";

            String progress = progChar;
            int b1 = (int) (percent * width);
            for (int i = 0; i < b1; i++) {
                progress += progChar;
            }
            //progress += ">";
            int b2 = width - b1;
            for (int i = 0; i < b2; i++) {
                progress += " ";
            }
            progress += " " + ((int) (percent * 1000)) / 10f + "%";

            return progress;
        }

        @Override
        public String getBarHeader(int width) {
            String head = "|0 ";
            width -= 10;

            width /= 2;

            for (int i = 0; i < width; i++){
                head += "-";
            }

            head += " 50 ";

            for (int i = 0; i < width; i++){
                head += "-";
            }

            head += " 100%|";

            return head;
        }
    }
}
