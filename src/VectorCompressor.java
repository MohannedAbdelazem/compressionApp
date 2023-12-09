import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

class QuantizedImage{
    public BufferedImage[] img;
    public double ErrorPercentage;
    QuantizedImage(BufferedImage[] img, double ErrorPercentage){
        this.img = img;
        this.ErrorPercentage = ErrorPercentage;
    }
}
public class VectorCompressor {
    public BufferedImage[] splitImage(BufferedImage im, int blockSize){
        int width = im.getWidth();
        int height = im.getHeight();
        int xBlocks = width/blockSize;
        int yBlocks = height/blockSize;

        BufferedImage[] result = new BufferedImage[xBlocks*yBlocks];
        for(int i = 0;i<xBlocks;i++){
            for(int j = 0;j<yBlocks;j++){
                int x = i*blockSize;
                int y = j*blockSize;

                BufferedImage block = im.getSubimage(x, y, blockSize, blockSize);
                result[i*yBlocks + j] = block;
            }
        }

        return result;
    }
    public void saveBlocks(BufferedImage[] blocks, String outputDirectoryPath) {
        File outputDirectory = new File(outputDirectoryPath);
        outputDirectory.mkdirs();

        for (int i = 0; i < blocks.length; i++) {
            try {
                String outputPath = outputDirectoryPath + "/block_" + (i + 1) + ".png";
                File outputFile = new File(outputPath);

                // Save the block as an image file
                ImageIO.write(blocks[i], "PNG", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BufferedImage[] generateCodeBook(BufferedImage[] images, int CodeBookSize){
        int numberOfImages = images.length;
        Random random = new Random();
        int[] RandomValues = new int[CodeBookSize];
        for(int i = 0;i< CodeBookSize;i++){
            RandomValues[i] = random.nextInt((numberOfImages-0) + 0);
            for(int k = 0;k<i;k++){
                if(RandomValues[k] == RandomValues[i]){
                    i--;
                    break;
                }
            }
        }
        BufferedImage[] CodeBook = new BufferedImage[CodeBookSize];
        for(int i = 0;i<CodeBookSize;i++){
            CodeBook[i] = images[RandomValues[i]];
        }
        return CodeBook;
    }

    public double CompareImages(BufferedImage img1, BufferedImage img2, int colorDifferenceThreshold) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int numberOfPixels = width * height;
        int similarPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p1 = img1.getRGB(x, y);
                int p2 = img2.getRGB(x, y);

                // Extract RGB components
                int r1 = (p1 >> 16) & 0xFF;
                int g1 = (p1 >> 8) & 0xFF;
                int b1 = p1 & 0xFF;

                int r2 = (p2 >> 16) & 0xFF;
                int g2 = (p2 >> 8) & 0xFF;
                int b2 = p2 & 0xFF;

                // Calculate color difference
                int colorDifference = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);

                // Check if the color difference is below the threshold
                if (colorDifference <= colorDifferenceThreshold) {
                    similarPixels++;
                }
            }
        }

        double similarityPercentage = (double) similarPixels / numberOfPixels * 100;
        return 100 - similarityPercentage; // Return the dissimilarity percentage
    }

    public QuantizedImage LBG(BufferedImage[] SplitImages, BufferedImage[] CodeBook){
        double ErrorPercentage = 0;
        int SplitImageIndex = 0;
        for(BufferedImage image: SplitImages){
            Double MinimumError = null;
            Integer MinImageIndex = null;
            for(int i = 0;i<CodeBook.length;i++){
                if(MinimumError == null){
                    MinimumError = CompareImages(CodeBook[i], image, 50);
                    MinImageIndex = i;
                    continue;
                }
                if(CompareImages(CodeBook[i], image, 50) < MinimumError){
                    MinimumError = CompareImages(CodeBook[i], image, 50);
                    MinImageIndex = i;
                }
            }
            if(MinimumError < 30){
                System.out.println("Swapped");
                ErrorPercentage+= MinimumError;
                SplitImages[SplitImageIndex] = CodeBook[MinImageIndex];
            }
            SplitImageIndex++;
        }
        QuantizedImage qi = new QuantizedImage(SplitImages, ErrorPercentage);
        return qi;
    }

    public QuantizedImage Compress(BufferedImage img, int blockSize, int CodeBookSize, int iterations){
        Integer MinIndex = null;
        BufferedImage[] SplitImage = splitImage(img, blockSize);
        ArrayList<QuantizedImage> quantizedImages= new ArrayList<>();
        for(int i = 0;i< iterations;i++){
            BufferedImage[] codeBook = generateCodeBook(SplitImage, CodeBookSize);
            QuantizedImage qi = LBG(SplitImage, codeBook);
            quantizedImages.add(qi);
            if(MinIndex == null){
                MinIndex = i;
            }
            else{
                if(quantizedImages.get(MinIndex).ErrorPercentage > qi.ErrorPercentage){
                    MinIndex = i;
                }
            }
        }

        return quantizedImages.get(MinIndex);
    }

    public BufferedImage combineImages(BufferedImage[] blocks, int originalWidth, int originalHeight, int blockSize) {
        int xBlocks = originalWidth / blockSize;
        int yBlocks = originalHeight / blockSize;

        BufferedImage result = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < xBlocks; i++) {
            for (int j = 0; j < yBlocks; j++) {
                int x = i * blockSize;
                int y = j * blockSize;

                BufferedImage block = blocks[i * yBlocks + j];
                result.getRaster().setPixels(x, y, blockSize, blockSize, block.getRaster().getPixels(0, 0, blockSize, blockSize, (int[]) null));
            }
        }

        return result;
    }
    public static void saveImageToBinaryFile(BufferedImage image, String filePath) {
        try {
            // Create a ByteArrayOutputStream to store the image bytes
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Write the image to the ByteArrayOutputStream in PNG format (you can choose a different format)
            ImageIO.write(image, "png", byteArrayOutputStream);

            // Get the byte array from the ByteArrayOutputStream
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Write the byte array to a binary file
            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                fileOutputStream.write(imageBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String compress(String path){

        try {
            BufferedImage image = ImageIO.read(new File(path));
            VectorCompressor vc = new VectorCompressor();
            // public QuantizedImage Compress(BufferedImage img, int blockSize, int CodeBookSize, int iterations){
            QuantizedImage qi = vc.Compress(image, 2, 20, 500);
//            vc.saveBlocks(qi.img, "./");
            System.out.printf("Error percentage is: %f\n", qi.ErrorPercentage);

            BufferedImage img = vc.combineImages(qi.img, image.getWidth(), image.getHeight(), 2);
            File outputFile = new File("output.png");
//            Save the block as an image file
            ImageIO.write(img, "PNG", outputFile);
            saveImageToBinaryFile(img,"./outputBin.bin");

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

}

