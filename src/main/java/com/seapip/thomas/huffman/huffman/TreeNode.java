package com.seapip.thomas.huffman.huffman;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * The {@code TreeNode} class consists of a left and right node and has constructor methods
 * to be simply created from a given byte array. It also has it's own serialization implementation
 * so it can be stored using less bytes.
 *
 * @author Thomas Gladdines
 * @see <a href="https://sonarcloud.io/dashboard?id=com.seapip.thomas.huffman%3AHuffman">Code analysis</a>
 * @since 1.8
 */
public class TreeNode implements Node {
    private Node leftNode;
    private Node rightNode;

    public TreeNode(byte[] bytes) {
        //Create bytes frequency map
        Map<Byte, Integer> map = new HashMap<>();
        for (byte b : bytes) map.put(b, map.getOrDefault(b, 0) + 1);

        //Create Huffman tree from character frequency map
        if (map.size() == 1) {
            leftNode = new ByteNode(bytes[0]);
            return;
        }
        Queue<Node> queue = new PriorityQueue<>(map.size(), (o1, o2) -> ((Integer) o1.getValue()).compareTo(o2.getValue()));
        for (Map.Entry entry : map.entrySet()) queue.add(new ByteNode((byte) entry.getKey(), (int) entry.getValue()));
        while (queue.size() > 1) queue.add(new TreeNode(queue.poll(), queue.poll()));

        //Set child node values of this tree to values from created Huffman tree
        leftNode = ((TreeNode) queue.peek()).getLeftNode();
        rightNode = ((TreeNode) queue.peek()).getRightNode();
    }

    public TreeNode(Node leftNode, Node rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public TreeNode(Queue<Byte> bytes, Collection<Boolean> structure) {
        TreeNode tree = (TreeNode) unflatten(bytes, new ArrayDeque<>(structure));
        if (tree != null) {
            leftNode = tree.getLeftNode();
            rightNode = tree.getRightNode();
        }
    }

    public static TreeNode read(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        byte[] data = new byte[4];

        //Read byte count
        dataInputStream.readFully(data, 0, 4);
        int count = ByteBuffer.wrap(data).getInt();

        //Read bytes
        Queue<Byte> bytes = new ArrayDeque<>(count);
        for (int i = 0; i < count; i++) {
            bytes.add(dataInputStream.readByte());
        }

        //Read structure size
        dataInputStream.readFully(data, 0, 4);
        int size = ByteBuffer.wrap(data).getInt();

        //Read structure bytes
        byte[] structureBytes = new byte[(int) Math.ceil(size / 8.0)];
        dataInputStream.readFully(structureBytes, 0, structureBytes.length);

        //Convert structure bytes to bits
        BitQueue structure = new BitQueue();
        for (byte b : structureBytes) for (int mask = 1; mask != 256; mask <<= 1) structure.add((b & mask) != 0);

        //Create Huffman tree from bytes and tree structure
        return new TreeNode(bytes, structure);
    }

    @Override
    public void flatten(Collection<Byte> bytes, Collection<Boolean> structure) {
        structure.add(false);
        if (leftNode != null) leftNode.flatten(bytes, structure);
        if (rightNode != null) rightNode.flatten(bytes, structure);
    }

    private Node unflatten(Queue<Byte> bytes, Queue<Boolean> structure) {
        if (bytes.isEmpty()) return null;
        return structure.poll() ? new ByteNode(bytes.poll()) : new TreeNode(unflatten(bytes, structure), unflatten(bytes, structure));
    }

    @Override
    public int getValue() {
        return leftNode.getValue() + rightNode.getValue();
    }

    public Node getLeftNode() {
        return leftNode;
    }

    public Node getRightNode() {
        return rightNode;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        Queue<Byte> bytes = new ArrayDeque<>();
        BitQueue structure = new BitQueue();

        //Get bytes and tree structure in pre order tree traversal
        flatten(bytes, structure);

        //Write bytes count
        outputStream.write(ByteBuffer.allocate(4).putInt(bytes.size()).array());

        //Write bytes
        for (Byte b : bytes) outputStream.write(b);

        //Write structure size
        outputStream.write(ByteBuffer.allocate(4).putInt(structure.size()).array());

        //Write tree structure
        outputStream.write(structure.toByteArray());
    }

    @Override
    public void toMap(Map<Byte, Collection<Boolean>> map, Collection<Boolean> bits) {
        if (leftNode != null) leftNode.toMap(map, copyAndAdd(bits, false));
        if (rightNode != null) rightNode.toMap(map, copyAndAdd(bits, true));
    }

    private Collection<Boolean> copyAndAdd(Collection<Boolean> bits, boolean bit) {
        Collection<Boolean> collection = new ArrayDeque<>(bits);
        collection.add(bit);
        return collection;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder().append("Huffman tree:\r\n|\r\n");
        toString(stringBuilder, new StringBuilder(), true);
        return stringBuilder.toString();
    }

    @Override
    public void toString(StringBuilder stringBuilder, StringBuilder prefix, boolean isTail) {
        if (rightNode != null) rightNode.toString(stringBuilder, newString(prefix, isTail), false);
        stringBuilder.append(prefix).append("|---|\r\n");
        if (leftNode != null) leftNode.toString(stringBuilder, newString(prefix, !isTail), true);
    }

    private StringBuilder newString(StringBuilder prefix, boolean isTail) {
        return new StringBuilder().append(prefix).append(isTail ? "|   " : "    ");
    }
}
