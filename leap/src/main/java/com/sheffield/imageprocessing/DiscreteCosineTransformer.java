package com.sheffield.imageprocessing;

import java.util.Arrays;

/**
 * Created by thomas on 02/03/2016.
 */
public class DiscreteCosineTransformer {

    private double[] originalImage;
    private int imageWidth = 0;
    private int imageHeight = 0;

    private int newWidth = 0;
    private double[] dctImage;
    private double[] coefficients;

    private static final double ONE_OVER_ROOT_TWO = 1d/ Math.sqrt(2);

    private int blocks = 0;

    public DiscreteCosineTransformer(double[] image, int width, int height, int blocks){
        originalImage = Arrays.copyOf(image, image.length);

        if (originalImage.length != width * height){
            throw new IllegalArgumentException("Size of image array must be equal to width * height");
        }

        imageWidth = width;
        imageHeight = height;
        newWidth = imageWidth - blocks;

        this.blocks = blocks;
    }

    public void calculateDct(){

        coefficients = new double[originalImage.length];

        for (int i = 0; i < imageWidth/blocks; i++) {
            for (int j = 0; j < imageHeight/blocks; j++) {
                    dct(i * blocks, j * blocks);
            }
        }


    }

    private int i(int x, int y){
        return (y * imageWidth) + x;
    }

    private double c(int u){
        return u != 0 ? 1 : ONE_OVER_ROOT_TWO;
    }

    public double[] inverse(){
        if (coefficients == null){
            calculateDct();
        }

        double[] newImage = new double[coefficients.length];

        for (int i = 0; i < imageWidth/blocks; i++){
            for (int j = 0; j < imageHeight/blocks; j++){
                    idct(i * blocks, j * blocks, newImage);
            }
        }



        return newImage;

    }

    private void idct(int xpos, int ypos, double[] newImage){
        for (int x = 0; x < blocks; x++){
            for (int y = 0; y < blocks; y++){
                double value = 0;
                for (int i = 0; i < blocks; i++){
                    for (int j = 0; j < blocks; j++){
                        value += (c(i) * c(j)) * coefficients[i(i+xpos, j+ypos)] *
                                Math.cos(i*Math.PI*((2*x+1)/(2*blocks))) *
                                Math.cos(j*Math.PI*((2*y+1)/(2*blocks)));
                    }
                }
                value = value / 4d;
//                if (value > 255)
//                    value = 255;
//                else if (value < 0)
//                    value = 0;
                newImage[i(xpos+x, ypos+y)] = value;
            }
        }
    }

    private void dct(int xpos, int ypos){
        for (int i = 0; i < blocks; i++){
            for (int j = 0; j < blocks; j++){
                double value = 0;
                for (int x = 0; x < blocks; x++){
                    for (int y = 0; y < blocks; y++){
                        value += originalImage[i(xpos + x, ypos + y)] *
                                Math.cos(i*Math.PI*((2*x+1)/(2*blocks))) *
                                Math.cos(j*Math.PI*((2*y+1)/(2 * blocks)));
                    }
                }
                coefficients[i(xpos+i, ypos+j)] = c(i) * c(j) * value / 4d;
            }
        }
    }
}
