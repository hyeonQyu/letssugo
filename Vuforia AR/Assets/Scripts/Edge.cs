using System.Collections.Generic;

public class Edge
{
    private Node start;
    private Node end;

    public List<Node> smallNodes; // BigNode 사이의 Node들 리스트

    public Edge(Node start, Node end)
    {
        this.start.nodeName = start.nodeName;
        this.end.nodeName = end.nodeName;
        smallNodes = new List<Node>();
    }

    public bool compareStartEnd(Node start, Node end)
    {
        return this.start.nodeName == start.nodeName && this.end.nodeName == end.nodeName;
    }


}
