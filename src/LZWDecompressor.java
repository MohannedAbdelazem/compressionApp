import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LZWDecompressor implements decompressionStrategy{
    @Override
    public String decompress(String line) {
        String result = "";
        Map<Integer, String> dictionary = new HashMap<>();

        // Initialize the dictionary with single characters (0-255).
        for (int i = 0; i <= 255; i++) {
            dictionary.put(i, String.valueOf((char) i));
        }

        ArrayList<Integer> data = new ArrayList<>();
        String[] input = line.substring(1, line.length() - 1).split(", ");
        for (String entry : input) {
            data.add(Integer.valueOf(entry));
        }

        int prevCode = -1;
        int currentCode;
        String currentEntry;
        String prevEntry = "";

        for (int i = 0; i < data.size(); i++) {
            currentCode = data.get(i);

            if (!dictionary.containsKey(currentCode)) {
                currentEntry = prevEntry + prevEntry.charAt(0);
            } else {
                currentEntry = dictionary.get(currentCode);
            }

            result += currentEntry;

            if (prevCode != -1) {
                dictionary.put(dictionary.size(), prevEntry + currentEntry.charAt(0));
            }

            prevCode = currentCode;
            prevEntry = currentEntry;
        }

        return result;
    }

//    public static void main(String[] args) {
//        LZWDecompressor decompressor = new LZWDecompressor();
//        System.out.println(decompressor.decompress("[65, 66, 65, 256, 256, 257, 259, 262, 258, 257, 66, 266, 267, 266, 99, 100]"));
//    }
}
