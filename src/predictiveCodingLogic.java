package com.example.compressionapp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class predictiveCodingLogic {
    public BufferedImage[] splitImage(BufferedImage im, int blockSize) {
        int width = im.getWidth();
        int height = im.getHeight();
        int xBlocks = width / blockSize;
        int yBlocks = height / blockSize;

        BufferedImage[] result = new BufferedImage[xBlocks * yBlocks];
        for (int i = 0; i < xBlocks; i++) {
            for (int j = 0; j < yBlocks; j++) {
                int x = i * blockSize;
                int y = j * blockSize;

                BufferedImage block = im.getSubimage(x, y, blockSize, blockSize);
                result[i * yBlocks + j] = block;
            }
        }

        return result;
    }

    public int[][] convertImageTo2DArray(BufferedImage image) {

        // Get the dimensions of the image
        int width = image.getWidth();
        int height = image.getHeight();

        // Convert the image to a 2D array
        int[][] result = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = image.getRGB(j, i) & 0xFF; // Get the grayscale value
            }
        }

        return result;
    }


    public int[][] predictor(int TheImage[][], int width, int height) {
        int[][] result = new int[height][width];
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                int A = TheImage[i][j - 1];
                int B = TheImage[i - 1][j - 1];
                int C = TheImage[i - 1][j];
                if (B <= Math.min(A, C)) {
                    result[i][j] = Math.max(A, C);
                } else if (B >= Math.max(A, C)) {
                    result[i][j] = Math.min(A, C);
                } else {
                    result[i][j] = A + C - B;
                }

            }
        }
        return result;
    }

    public int[][] difference(int[][] image, int width, int height) {
        int[][] predictedImage = predictor(image, width, height);
        int[][] result = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = image[i][j] - predictedImage[i][j];
            }
        }

        return result;
    }

    public int[][] quantize(int[][] values, int numLevels) {
        int width = values.length;
        int height = values[0].length;
        int[][] quantizedValues = new int[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int originalValue = values[i][j];

                // Perform quantization
                int quantizedValue = (originalValue * (numLevels - 1)) / 255;

                quantizedValues[i][j] = quantizedValue;
            }
        }

        return quantizedValues;
    }

    public int[][] quantize(int[][] difference, int height, int width) {
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                int value = difference[i][j];

                // Quantize the value
                int quantizedValue = quantizeValue(value);

                // Update the difference array with quantized value
                difference[i][j] = quantizedValue;
            }
        }

        return difference;
    }

    //Ziad wrote this:
    private int quantizeValue(int value) {
        int CLASS_RANGE = 32;
        int NUM_CLASSES = 16;
        // Ensure that the value is within the range [-255, 255]
        value = Math.max(-255, Math.min(255, value));

        // Map the value to the corresponding quantization level
        int quantizationLevel = (value + 255) / CLASS_RANGE;

        // Ensure that the quantization level is within the range [0, NUM_CLASSES - 1]
        quantizationLevel = Math.max(0, Math.min(NUM_CLASSES - 1, quantizationLevel));

        // Map the quantization level back to the quantized value
        //int quantizedValue = quantizationLevel * CLASS_RANGE - 255;

        return quantizationLevel;
    }

    public void compressor(String filePath) {
        try {
            // Load the image using ImageIO
            BufferedImage image = ImageIO.read(new File(filePath));
            if (image == null) {
                System.err.println("Failed to read the image.");
                return;
            }

            int[][] Image = convertImageTo2DArray(image);
            int[][] predicted = predictor(Image, image.getWidth(), image.getHeight());
            int[][] diff = difference(Image, image.getWidth(), image.getHeight());
            int[][] quantized = quantize(diff, image.getHeight(), image.getWidth());

            // Write to binary file
            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("output.bin"))) {
                // Write width and height
                outputStream.writeInt(image.getWidth());
                outputStream.writeInt(image.getHeight());

                // Write predictor array
                for (int i = 0; i < image.getHeight(); i++) {
                    for (int j = 0; j < image.getWidth(); j++) {
                        outputStream.writeInt(predicted[i][j]);
                    }
                }

                // Write difference array
                for (int i = 0; i < image.getHeight(); i++) {
                    for (int j = 0; j < image.getWidth(); j++) {
                        outputStream.writeInt(quantized[i][j]);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int[][] dequantize(int[][] quantizedDifference, int height, int width) {
        for (int i = 1; i < height; i++) {
            for (int j = 1; j < width; j++) {
                int quantizedValue = quantizedDifference[i][j];

                // Dequantize the value
                int originalValue = dequantizeValue(quantizedValue);

                // Update the quantized difference array with dequantized value
                quantizedDifference[i][j] = originalValue;
            }
        }

        return quantizedDifference;
    }

    // Ziad wrote this:
    private int dequantizeValue(int quantizedValue) {
        int CLASS_RANGE = 32;
        int NUM_CLASSES = 16;

        // Map the quantization level back to the original value range
        int originalValue = quantizedValue * CLASS_RANGE - 255;

        return originalValue;
    }

    public void decompressor(String filePath) {
        try {
            // Read from the binary file
            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(filePath))) {
                // Read width and height
                int width = inputStream.readInt();
                int height = inputStream.readInt();

                // Read predictor array
                int[][] predicted = new int[height][width];
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        predicted[i][j] = inputStream.readInt();
                    }
                }

                // Read difference array

                int[][] quantizedDiff = new int[height][width];
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        quantizedDiff[i][j] = inputStream.readInt();

                    }
                }

                // Perform further processing or visualization if needed
                int[][] dequantizeddiff = dequantize(quantizedDiff, height, width);

                int[][] result = new int[height][width];
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        result[i][j] = predicted[i][j] + dequantizeddiff[i][j];
                    }
                }

                // Convert the result to a BufferedImage
                BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int pixelValue = result[i][j] << 16 | result[i][j] << 8 | result[i][j];
                        outputImage.setRGB(j, i, pixelValue);
                    }
                }

                // Save the output image
                ImageIO.write(outputImage, "png", new File("output.png"));


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}