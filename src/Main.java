import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.*;



import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.FileWriter;

public class Main extends Application{

    ArrayList<String> ReadFileForLZ77(String FileName){
        ArrayList<String> result = new ArrayList<>();
        try(Scanner scanner = new Scanner(new File(FileName))){
            while(scanner.hasNextLine()){
                result.add(scanner.nextLine());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    ArrayList<String> ReadFile(String FileName){
        ArrayList<String> result = new ArrayList<>();
        try(Scanner scanner = new Scanner(new File(FileName))){
            while(scanner.hasNextLine()){
                result.add(scanner.nextLine());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    String ReadFromBinaryFile(String FileName){
        String Data = "";
        File file = new File(FileName);
        try(FileInputStream fis = new FileInputStream(file)){
            int content;

            while((content = fis.read()) != -1){
                Data += (char) content;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return Data;
    }

    void SaveToBinaryFile(String data){
        File file = new File("compressed.bin");
        try(FileOutputStream fos = new FileOutputStream(file, true)){
                fos.write(data.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage){

        VBox VBForWelcome = new VBox();
        Label l = new Label("Welcome to our compression app");
        VBForWelcome.getChildren().add(l);
        l.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS';");
        VBForWelcome.setAlignment(Pos.CENTER);
        VBForWelcome.setStyle("-fx-background-image: url('welcomePic.jpg');");
        VBForWelcome.setFillWidth(true);
        Scene welcomeScene = new Scene(VBForWelcome);
        stage.setScene(welcomeScene);
        stage.setFullScreen(true);
        stage.show();
        try {
            Thread.sleep(3000);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        stage.setFullScreen(false);
        VBox App = new VBox();
        stage.setWidth(500);
        stage.setHeight(500);

        Label WelcomeTitle = new Label("Welcome to the compression app");

        App.getChildren().add(WelcomeTitle);
        App.setAlignment(Pos.TOP_CENTER);
        Label entryLabel = new Label("Please enter the path of the file");
        App.getChildren().add(entryLabel);
        entryLabel.setStyle("-fx-translate-y: 60px; -fx-translate-x: -40px; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS';");
        WelcomeTitle.setStyle("-fx-translate-y: 20px; -fx-font-size: 20px; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS';");
        TextField entry = new TextField();
        entry.setPrefWidth(300);
        entry.setMaxWidth(300);
        entry.setStyle("-fx-translate-y: 50px;");
        ChoiceBox<String> compressionTechnique = new ChoiceBox<>();
        compressionTechnique.getItems().addAll("LZ77", "LZW");
        compressionTechnique.setValue("-");
        compressionTechnique.setStyle("-fx-translate-y: 60px");
        Label ChoiceBoxLabel = new Label("compression technique:");
        App.getChildren().add(ChoiceBoxLabel);
        ChoiceBoxLabel.setStyle("-fx-translate-y: 110px; -fx-translate-x: -100px; -fx-font-weight: bold; -fx-font-family: 'Comic Sans MS';");
        App.getChildren().add(entry);
        App.getChildren().add(compressionTechnique);
        Scene appScene = new Scene(App);
        Button CompressButton = new Button("Compress");
        CompressButton.setOnMouseClicked(event ->{
            if(!compressionTechnique.getValue().equals("-")) {
                File f = new File(entry.getText());
                if(f.exists()) {
                    compressionStrategy TheCompressor;
                    if (compressionTechnique.getValue().equals("LZ77")) {
                        TheCompressor = new compressor();
                        ArrayList<String> elements =ReadFileForLZ77(entry.getText());
                        for(String element: elements){
                            String t = TheCompressor.compress(element);
                            SaveToBinaryFile(t);
                        }
                    } else {
                        TheCompressor = new LZWCompressor();


                        String fileDataInLines = "";
                        ArrayList<String> fileData = ReadFile(entry.getText());
                        for (String line : fileData) {
                            fileDataInLines += line + "\n";
                        }
                        String outputFromCompression = TheCompressor.compress(fileDataInLines);
                        SaveToBinaryFile(outputFromCompression);
                    }
                    entry.clear();
                    compressionTechnique.setValue("-");

                }
                else{
                    System.out.println("Please choose an existent file");
                }
            }
            else{
                System.out.println("Please choose between LZW and LZ77");
            }
        });
        Button DecompressButton = new Button("Decompress");

        DecompressButton.setOnMouseClicked(event ->{
            if(!compressionTechnique.getValue().equals("-")) {
                File f = new File(entry.getText());
                if(f.exists()) {
                    decompressionStrategy TheDecompressor;
                    if (compressionTechnique.getValue().equals("LZW")) {
                        TheDecompressor = new LZWDecompressor();
                    } else {
                        TheDecompressor = new decompressor();
                    }
                    String dataToBeDecompressed = ReadFromBinaryFile(entry.getText());
                    String decompressedData = TheDecompressor.decompress(dataToBeDecompressed);
                    try (PrintWriter writer = new PrintWriter(new FileWriter("decompressed.txt"))) {
                        writer.print(decompressedData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    entry.clear();
                    compressionTechnique.setValue("-");
                }
                else{
                    System.out.println("Please choose an existent file");
                }
            }
            else{
                System.out.println("Please choose between LZW and LZ77");
            }
        });


        App.getChildren().addAll(CompressButton, DecompressButton);
        CompressButton.setStyle("-fx-translate-y: 200px; -fx-translate-x: -50px");
        DecompressButton.setStyle("-fx-translate-y:175px; -fx-translate-x: 50px;");

        stage.setScene(appScene);

    }
//text field is called entry,choicebox is called compressionTechnique
}