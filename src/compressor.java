import java.util.ArrayList;

public class compressor implements compressionStrategy{

    @Override
    public String compress(String line) {
        ArrayList<tag> tags = new ArrayList<>();
        String searchBuffer = "";
        String LookAheadBuffer = "";
        int destination = 0;
        int length = 0;
        char nextCharacter = '/'; /* null character will be marked as / */
        for (int i = 0; i < line.length(); i++) {
            nextCharacter = line.charAt(i);
            Boolean flag = true;
            LookAheadBuffer = LookAheadBuffer.concat(line.substring(i, i+1));
            for (int j = i - LookAheadBuffer.length(); j >= 0; j--) {
                if(searchBuffer.length() >= LookAheadBuffer.length() && ((j + LookAheadBuffer.length()) <= searchBuffer.length())) {
                    if (searchBuffer.substring(j, j + LookAheadBuffer.length()).equals(LookAheadBuffer)) {
                        length++;
                        destination = searchBuffer.substring(searchBuffer.lastIndexOf(searchBuffer.substring(j, j+LookAheadBuffer.length()))).length();
                        flag = false;
                        break;
                    }
                }

            }
            if ((i == line.length() - 1 && flag == false)) {
                flag = true;
                nextCharacter = '/';
            }
            if(LookAheadBuffer.length() == 7 ){
                flag = true;
            }
            if (flag == false) {
                continue;
            } else {
                searchBuffer = searchBuffer.concat(LookAheadBuffer);
                while(searchBuffer.length()> 7){
                    searchBuffer = searchBuffer.substring(1);
                }
                tags.add(new tag(destination, length, nextCharacter));
                destination = 0;
                length = 0;
                nextCharacter = '/';
                LookAheadBuffer = "";
            }
        }
        System.out.println("Compressed data: " + tags.toString() + "         The end of them");
        return tags.toString();
    }

//    public static void main(String[] args) {
//        compressor comp = new compressor();
//        ArrayList<tag> tags = comp.compress("Mohanned");
//        for(tag t: tags){
//            System.out.print(t.toString());
//        }
//    }
}
