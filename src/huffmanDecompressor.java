import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import static java.util.Objects.requireNonNull;

public class huffmanDecompressor implements decompressionStrategy{

    @Override
    public String decompress(String encodedText) {
        Node root;
        Map<Character, Integer> charFrequencies;
        Map<Character, String> codes = new HashMap<>();
        Queue<Node> queue = new PriorityQueue<>();
        charFrequencies = new HashMap<>();

        // Extract the size of the overhead
        int i;
        StringBuilder encodedBufferSize = new StringBuilder();
        for (i = 0; i < encodedText.length(); i++) {
            if (!(encodedText.charAt(0) >= '0' && encodedText.charAt(0) <= '9')) {
                break;
            }
            encodedBufferSize.append(encodedText.charAt(0));
            encodedText = encodedText.substring(1);
        }
        int encodedBufferSizeInt = Integer.parseInt(encodedBufferSize.toString());

        // Extract the encoded buffer from the end
        String encodedBuffer = encodedText.substring(encodedText.length() - encodedBufferSizeInt);
        encodedText = encodedText.substring(0, encodedText.length() - encodedBufferSizeInt);

        // Extract character frequencies from the overhead
        while (!encodedText.isEmpty()) {
            char character = encodedText.charAt(0);
            encodedText = encodedText.substring(1);

            int j;
            StringBuilder freq = new StringBuilder();
            for (j = 0; j < encodedText.length(); j++) {
                if (!(encodedText.charAt(j) >= '0' && encodedText.charAt(j) <= '9')) {
                    break;
                }
                freq.append(encodedText.charAt(j));
            }
            int frequency = Integer.parseInt(freq.toString());
            charFrequencies.put(character, frequency);
            // Move the pointer after the frequency digits
            encodedText = encodedText.substring(j);
        }
        // Rebuild the Huffman tree
        for (Map.Entry<Character, Integer> entry : charFrequencies.entrySet()) {
            queue.add(new Leaf(entry.getKey(), entry.getValue()));
        }

        while (queue.size() > 1) {
            queue.add(new Node(queue.poll(), requireNonNull(queue.poll())));
        }

        root = queue.poll();

        // Decode the encoded buffer using the Huffman tree
        StringBuilder sb = new StringBuilder();
        Node current = root;
        for (char character : encodedBuffer.toCharArray()) {
            current = character == '0' ? current.getLeftNode() : current.getRightNode();
            if (current instanceof Leaf && ((Leaf) current).getCharacter() != 0) {
                sb.append(((Leaf) current).getCharacter());
                current = root;
            }
        }
        return sb.toString();
    }




    private Node buildHuffmanTree(Map<Character, Integer> charFrequencies) {
        Queue<Node> queue = new PriorityQueue<>();

        charFrequencies.forEach((character, frequency) ->
                queue.add(new Leaf(character, frequency))
        );

        while (queue.size() > 1) {
            queue.add(new Node(queue.poll(), queue.poll()));
        }

        return queue.poll(); // Return the root of the Huffman tree
    }


}
