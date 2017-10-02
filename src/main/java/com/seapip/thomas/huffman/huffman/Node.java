package com.seapip.thomas.huffman.huffman;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;

/**
 * The {@code Node} interface is implemented by both the {@code TreeNode} and the {@code ByteNode}.
 *
 * @author Thomas Gladdines
 * @see <a href="https://sonarcloud.io/dashboard?id=com.seapip.thomas.huffman%3AHuffman">Code analysis</a>
 * @since 1.8
 */
public interface Node {
    int getValue();

    void flatten(Collection<Byte> bytes, Collection<Boolean> structure);

    void toMap(Map<Byte, Collection<Boolean>> map, Collection<Boolean> bits);

    void toString(StringBuilder stringBuilder, StringBuilder prefix, boolean isTail);

    void write(OutputStream outputStream) throws IOException;
}
