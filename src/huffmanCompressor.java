import java.util.*;

import static java.util.Objects.requireNonNull;


class Node implements Comparable<Node> {
    private int frequency;
    Node leftNode;
    Node rightNode;

    public int getFrequency() {
        return this.frequency;
    }

    public Node getLeftNode() {
        return this.leftNode;
    }

    public Node getRightNode() {
        return this.rightNode;
    }

    public Node(Node leftNode, Node rightNode) {
        this.frequency = leftNode.getFrequency() + rightNode.getFrequency();
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public Node(int frequency) {
        this.frequency = frequency;
        rightNode = null;
        leftNode = null;
    }

    @Override
    public int compareTo(Node node) {
        return Integer.compare(this.frequency, node.getFrequency());
    }
}

class Leaf extends Node {
    private char character;

    public Leaf(char character, int frequency) {
        super(frequency);
        this.character = character;
    }

    public char getCharacter() {
        return character;
    }
}

public class huffmanCompressor implements compressionStrategy{



@Override
    public String compress(String text) {
        Node root;
        Map<Character, Integer> charFrequencies;
        Map<Character, String> codes = new HashMap<>();
        Queue<Node> queue = new PriorityQueue<>();
        charFrequencies = new HashMap<>();
        for (char character : text.toCharArray()) {
            charFrequencies.put(character, charFrequencies.getOrDefault(character, 0) + 1);
        }

        charFrequencies.forEach((character, frequency) ->
                queue.add(new Leaf(character, frequency))
        );


        while (queue.size() > 1) {
            queue.add(new Node(queue.poll(), requireNonNull(queue.poll())));
        }

        root = queue.poll(); // Set the root

        codes = generateHuffmanCodes(root);
        StringBuilder sb = new StringBuilder();
        for (char character : text.toCharArray()) {
            sb.append(codes.get(character));
        }

        StringBuilder overHead = new StringBuilder();
        overHead.append(sb.length());
        charFrequencies.forEach((character, integer) ->
                overHead.append(character.toString().concat(Integer.toString(integer)))
        );

        return overHead.append(sb.toString()).toString();

    }

    private Map<Character, String> generateHuffmanCodes(Node rootNode) {
        Map<Character, String> generatedCodes = new HashMap<>();
        Stack<Node> stack = new Stack<>();
        stack.push(rootNode);
        Map<Node, String> currentCodes = new HashMap<>();

        while (!stack.isEmpty()) {
            Node node = stack.pop();

            if (node instanceof Leaf && ((Leaf) node).getCharacter() != 0) {
                generatedCodes.put(((Leaf) node).getCharacter(), currentCodes.get(node));
                continue;
            }

            if (node.getRightNode() != null) {
                stack.push(node.getRightNode());
                currentCodes.put(node.getRightNode(), currentCodes.getOrDefault(node, "") + "1");
            }

            if (node.getLeftNode() != null) {
                stack.push(node.getLeftNode());
                currentCodes.put(node.getLeftNode(), currentCodes.getOrDefault(node, "") + "0");
            }
        }
        return generatedCodes;
    }






//    public static void main(String[] args) {
//        String inputText = "moha moha moah\nabdobeedo";
//        huffmanCompressor compressor = new huffmanCompressor();
//        String encodedText = compressor.encode(inputText);
//
//        System.out.println("Original text: " + inputText);
//        System.out.println("Encoded text: " + encodedText);
//
//        huffmanDecompressor decompressor = new huffmanDecompressor();
//        String decodedText = decompressor.decode(encodedText);
//        System.out.println("Decoded text: " + decodedText);
//    }
}
