import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class LZWCompressor implements compressionStrategy{
    @Override
    public String compress(String Line){
        Map<Integer, String> dictionary = new HashMap<>();
        ArrayList<Integer> result = new ArrayList<>();
        String searchBuffer = "";
        for(int i = 0;i<Line.length();i++){
            searchBuffer += Line.charAt(i);
            if(searchBuffer.length() == 1){
                continue;
            }
            else{
                boolean flag = false; /* If the flag is true it refers that he did find a match */
                for(Map.Entry<Integer, String> item: dictionary.entrySet()){
                    if(item.getValue().equals(searchBuffer)){
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    continue;
                }
                else{
                    dictionary.put(256+dictionary.size(), searchBuffer.substring(0));
                    if(searchBuffer.length() == 2){
                        result.add((int)searchBuffer.charAt(0));
                        searchBuffer = "";
                        i--;
                    }
                    else{
                        for(Map.Entry<Integer, String> item: dictionary.entrySet()){
                            if(item.getValue().equals(searchBuffer.substring(0, searchBuffer.length()-1))){
                                result.add(item.getKey());
                                searchBuffer = "";
                                i--;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(!searchBuffer.isEmpty()){
            if(searchBuffer.length() == 1){
                result.add((int) searchBuffer.charAt(0));
            }
            else{
                for(Map.Entry<Integer, String> item : dictionary.entrySet()){
                    if(item.getValue().equals(searchBuffer)){
                        result.add(item.getKey());
                        break;
                    }
                }
            }
        }
        return result.toString();
    }

//    public static void main(String[] args){
//        String integers;
//        LZWCompressor compressor = new LZWCompressor();
////        integers = compressor.compress("ABAABABBAABAABAAAABABBBBBBBBcd");
//        integers = compressor.compress("ABAABABBAABAABAAAABABBBBBBBBcd");
//        System.out.println(integers);
//    }
}
