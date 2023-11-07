import java.util.ArrayList;
public class decompressor implements decompressionStrategy{
    @Override
    public String decompress(String line) {
        // fLine will remove the tags '<' and '>'
        String[] fLine = line.substring(2, line.length() - 2).split(">, <");
        ArrayList<tag> tags = new ArrayList<>();
        for (String Line : fLine) {
            String[] components = Line.split(",");
            int destination = Integer.parseInt(components[0]);
            int length = Integer.parseInt(components[1]);
            char nextCharacter = components[2].charAt(0);
            tags.add(new tag(destination, length, nextCharacter));
        }
        int size = tags.size();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (tags.get(i).getDestination() == 0) {
                result.append(tags.get(i).getNextCharacter());
            } else {
                int startIndex = result.length() - tags.get(i).getDestination();
                int endIndex = startIndex + tags.get(i).getLength();
                String substring = result.substring(startIndex, endIndex);
                result.append(substring);
                result.append(tags.get(i).getNextCharacter());
            }
        }
        String decompressedData = result.toString();
        return decompressedData;
    }
}
