package org.jamon.nodegen;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class NodesParser
{
    private NodesParser() {}

    public static Iterable<NodeDescriptor> parseNodes(
        Reader p_nodesDescriptor) throws IOException
    {
        LineNumberReader reader = new LineNumberReader(p_nodesDescriptor);
        String line;
        Map<String, NodeDescriptor> nodes = 
            new HashMap<String, NodeDescriptor>();

        while ((line = reader.readLine()) != null)
        {
            line.trim();
            if (!line.startsWith("#") && line.length() > 0)
            {
                NodeDescriptor node = new NodeDescriptor(line, nodes); 
                nodes.put(node.getName(), node);
            }
        }
        return nodes.values();
    }

}
