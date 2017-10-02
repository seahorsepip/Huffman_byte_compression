package com.seapip.thomas.huffman.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * The {@code ByteNode} class consists of a single byte and it's frequency
 * and is used in combination with the {@code TreeNode} class.
 *
 * @author Thomas Gladdines
 * @see <a href="https://sonarcloud.io/dashboard?id=com.seapip.thomas.huffman%3AHuffman">Code analysis</a>
 * @since 1.8
 */
public class ByteNode implements Node {
    private byte b;
    private int value;

    public ByteNode(byte b) {
        this.b = b;
    }

    public ByteNode(byte b, int value) {
        this.b = b;
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void flatten(Collection<Byte> bytes, BitQueue structure) {
        structure.add(true);
        bytes.add(b);
    }

    @Override
    public void toMap(Map<Byte, Collection<Boolean>> map, BitQueue bits) {
        map.put(b, bits);
    }

    @Override
    public String toString() {
        return String.valueOf((int) b);
    }

    @Override
    public void toString(StringBuilder stringBuilder, StringBuilder prefix, boolean isTail) {
        stringBuilder.append(prefix)
                .append("|-\'")
                .append(toString().replace("\n", "\\n").replace("\r", "\\r"))
                .append("\'\r\n");
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(b);
    }
}
