import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

public class VectorCompressor{


    public static double[][] generateCodebook(double[][] data, int numCodevectors) {
        int numSamples = data.length;
        int numFeatures = data[0].length;

        double[][] codebook = new double[numCodevectors][numFeatures];

        // Initialize codebook with random vectors from the input data
        for (int i = 0; i < numCodevectors; i++) {
            int randomIndex = (int) (Math.random() * numSamples);
            codebook[i] = Arrays.copyOf(data[randomIndex], numFeatures);
        }

        // Run K-means clustering to update the codebook
        int iterations = 100;
        for (int iter = 0; iter < iterations; iter++) {
            int[] assignments = new int[numSamples];

            // Assign each data point to the nearest code vector
            for (int i = 0; i < numSamples; i++) {
                double minDistance = Double.MAX_VALUE;
                for (int j = 0; j < numCodevectors; j++) {
                    double distance = euclideanDistance(data[i], codebook[j]);
                    if (distance < minDistance) {
                        minDistance = distance;
                        assignments[i] = j;
                    }
                }
            }

            // Update code vectors based on the assigned data points
            for (int i = 0; i < numCodevectors; i++) {
                double[] sum = new double[numFeatures];
                int count = 0;
                for (int j = 0; j < numSamples; j++) {
                    if (assignments[j] == i) {
                        for (int k = 0; k < numFeatures; k++) {
                            sum[k] += data[j][k];
                        }
                        count++;
                    }
                }
                if (count > 0) {
                    for (int k = 0; k < numFeatures; k++) {
                        codebook[i][k] = sum[k] / count;
                    }
                }
            }
        }

        return codebook;
    }

    public static double[][] vectorQuantizationEncode(double[][] data, double[][] codebook) {
        int numSamples = data.length;
        int numCodevectors = codebook.length;
        int numFeatures = data[0].length;

        double[][] encodedData = new double[numSamples][numCodevectors];

        // Assign each data point to the nearest code vector
        for (int i = 0; i < numSamples; i++) {
            double minDistance = Double.MAX_VALUE;
            int minIndex = -1;
            for (int j = 0; j < numCodevectors; j++) {
                double distance = euclideanDistance(data[i], codebook[j]);
                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = j;
                }
            }
            encodedData[i][minIndex] = 1.0;
        }

        return encodedData;
    }

    public static double[][] vectorQuantizationDecode(double[][] encodedData, double[][] codebook) {
        int numSamples = encodedData.length;
        int numCodevectors = codebook.length;
        int numFeatures = codebook[0].length;

        double[][] decodedData = new double[numSamples][numFeatures];

        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < numCodevectors; j++) {
                for (int k = 0; k < numFeatures; k++) {
                    decodedData[i][k] += encodedData[i][j] * codebook[j][k];
                }
            }
        }

        return decodedData;
    }

    public static double euclideanDistance(double[] vector1, double[] vector2) {
        double sum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            sum += Math.pow(vector1[i] - vector2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static void saveEncodedDataToBinaryFile(double[][] encodedData, String fileName) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName))) {
            int numSamples = encodedData.length;
            int numCodevectors = encodedData[0].length;

            for (int i = 0; i < numSamples; i++) {
                for (int j = 0; j < numCodevectors; j++) {
                    dos.writeDouble(encodedData[i][j]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[][] loadEncodedDataFromBinaryFile(String fileName, int numSamples, int numCodevectors) {
        double[][] encodedData = new double[numSamples][numCodevectors];

        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            for (int i = 0; i < numSamples; i++) {
                for (int j = 0; j < numCodevectors; j++) {
                    encodedData[i][j] = dis.readDouble();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodedData;
    }

    public static void saveDecodedDataToImageFile(double[][] decodedData, String fileName, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grayValue = (int) (decodedData[i][j] * 255);
                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;
                image.setRGB(j, i, rgb);
            }
        }

        try {
            ImageIO.write(image, "jpg", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double calculateReconstructionError(double[][] originalData, double[][] decodedData) {
        int numSamples = originalData.length;
        int numFeatures = originalData[0].length;

        double sumSquaredError = 0.0;

        for (int i = 0; i < numSamples; i++) {
            for (int j = 0; j < numFeatures; j++) {
                sumSquaredError += Math.pow(originalData[i][j] - decodedData[i][j], 2);
            }
        }

        return Math.sqrt(sumSquaredError / (numSamples * numFeatures));
    }

    public static double[][] loadImageAsGrayArray(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            int width = image.getWidth();
            int height = image.getHeight();
            double[][] grayArray = new double[height][width];

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int rgb = image.getRGB(j, i);
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    int grayValue = (red + green + blue) / 3; // Simple conversion to grayscale
                    grayArray[i][j] = grayValue / 255.0; // Normalize to [0, 1]
                }
            }

            return grayArray;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void comp(String path){
        String imagePath = path;
        double[][] imageAsArray = loadImageAsGrayArray(imagePath);

        // Generate a codebook using K-means clustering
        int codebookSize = 500;
        double[][] codebook = generateCodebook(imageAsArray, codebookSize);

        // Encode the image data using vector quantization
        double[][] encodedData = vectorQuantizationEncode(imageAsArray, codebook);

        // Save the encoded data to a binary file
        saveEncodedDataToBinaryFile(encodedData, "encoded_data.bin");

        // Load the encoded data from the binary file (for demonstration)
        double[][] loadedEncodedData = loadEncodedDataFromBinaryFile("encoded_data.bin", imageAsArray.length, codebookSize);

        // Decode the loaded encoded data
        double[][] decodedData = vectorQuantizationDecode(loadedEncodedData, codebook);

        // Check the reconstruction error
        double reconstructionError = calculateReconstructionError(imageAsArray, decodedData);
        System.out.println("Reconstruction Error: " + reconstructionError);

        // Save the decoded data to a JPG file
        saveDecodedDataToImageFile(decodedData, "decoded_image.jpg", imageAsArray[0].length, imageAsArray.length);
    }
}
