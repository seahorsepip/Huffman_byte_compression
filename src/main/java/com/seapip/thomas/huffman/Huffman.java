package com.seapip.thomas.huffman;

import com.seapip.thomas.huffman.huffman.BitQueue;
import com.seapip.thomas.huffman.huffman.ByteNode;
import com.seapip.thomas.huffman.huffman.Node;
import com.seapip.thomas.huffman.huffman.TreeNode;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code Huffman} class consists exclusively of static methods to compress and decompress
 * text data using the Huffman algorithm.
 *
 * @author Thomas Gladdines
 * @see <a href="https://sonarcloud.io/dashboard?id=com.seapip.thomas.huffman%3AHuffman">Code analysis</a>
 * @since 1.8
 */
public final class Huffman {
    private Huffman() {
    }

    /**
     * Returns a compressed output stream for a given input stream
     *
     * @param inputStream  The data stream to read and compress
     * @param outputStream The data stream to write the compressed data too
     * @throws CompressionException Exception thrown when compressions fails
     */
    public static void compress(InputStream inputStream, OutputStream outputStream) throws CompressionException {
        try {
            //Start time
            long startTime = System.currentTimeMillis();

            //Read text from input stream
            byte[] bytes;
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    byteArrayOutputStream.write(data, 0, nRead);
                }
                bytes = byteArrayOutputStream.toByteArray();
            }

            //Throw compression exception if content length is zero
            if (bytes.length == 0) throw new CompressionException("Data can't be empty");

            //Output read time to console
            long readTime = System.currentTimeMillis();
            System.out.println("Read time: " + (readTime - startTime) + "ms"); //NOSONAR

            //Create Huffman tree
            TreeNode tree = new TreeNode(bytes);

            //Convert Huffman tree to map
            Map<Byte, Collection<Boolean>> map = new HashMap<>();
            tree.toMap(map, new ArrayDeque<>());

            //Encode bytes using map
            BitQueue bits = new BitQueue(1000000);
            for (byte b : bytes) bits.addAll(map.get(b));

            //Convert encoded data to byte array
            byte[] data = bits.toByteArray();

            //Output compression time to console
            long compressionTime = System.currentTimeMillis();
            System.out.println("Compression time: " + (compressionTime - readTime) + "ms"); //NOSONAR

            //Write Huffman tree
            tree.write(outputStream);

            //Write compressed data size in bits
            outputStream.write(ByteBuffer.allocate(4).putInt(bits.size()).array());

            //Write compressed data
            outputStream.write(data);

            //Output write time to console
            long writeTime = System.currentTimeMillis();
            System.out.println("Write time: " + (writeTime - compressionTime) + "ms"); //NOSONAR

            //Output total time to console
            System.out.println("Total time: " + (writeTime - startTime) + "ms"); //NOSONAR

            //Output Huffman tree to console
            System.out.println(tree.toString()); //NOSONAR
        } catch (IOException e) {
            throw new CompressionException(e.getMessage());
        }
    }

    /**
     * Returns an uncompressed output stream for a given compressed input stream
     *
     * @param inputStream  The compressed data stream
     * @param outputStream The data stream to write the decompressed data too
     * @throws CompressionException Exception thrown when decompression fails
     */
    public static void decompress(InputStream inputStream, OutputStream outputStream) throws CompressionException {
        try {
            //Start time
            long startTime = System.currentTimeMillis();

            //Read Huffman tree
            TreeNode tree = TreeNode.read(inputStream);

            DataInputStream dataInputStream = new DataInputStream(inputStream);
            byte[] data = new byte[8];

            //Read compressed data size in bits
            dataInputStream.readFully(data, 0, 4);
            int size = ByteBuffer.wrap(data).getInt();

            //Decode compressed data using Huffman tree
            Node node = tree;
            int offset = 0;
            for (int i = 0; i < Math.ceil(size / 8.0); i++) {
                byte b = dataInputStream.readByte();
                for (int mask = 1; mask != 256; mask <<= 1) {
                    if (offset >= size) break;
                    if (node instanceof ByteNode) {
                        node.write(outputStream);
                        node = tree;
                    }
                    offset++;
                    node = ((b & mask) != 0) ? ((TreeNode) node).getRightNode() : ((TreeNode) node).getLeftNode();
                }
            }

            //Output total time to console
            System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + "ms"); //NOSONAR
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw new CompressionException(e.getMessage());
        }
    }

    public static class CompressionException extends Exception {
        public CompressionException(String message) {
            super(message);
        }
    }
}