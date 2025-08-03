import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Graph {
    private class Node {
        public String name;
        public Object payload;
        private HashMap<String, Integer> edges;

        public boolean connect(String to, Integer weight) {
            if (!nodes.containsKey(to)) {
                return false;
            }
            Integer result = edges.put(to, weight);
            return (result == null);
        }

        public boolean disconnect(String to) {
            return (edges.remove(to) != null);
        }

        public boolean connected(String to) {
            return edges.containsKey(to);
        }

        public Integer edgeWeight(String to) {
            return edges.get(to);
        }

        public void changeNodeValue(Object value) {
            payload = value;
        }

        public void printNode() {
            System.out.printf("Node %s with value %s.\n", name,
                    payload.toString());
            for (String to : edges.keySet()) {
                Integer weight = edges.get(to);
                System.out.printf(" Connected to %s at weight %d.\n", to, weight);
            }
        }

        public Node(String newName, Object newPayload) {
            edges = new HashMap<String, Integer>();
            name = newName;
            payload = newPayload;
        }
    }

    HashMap<String, Node> nodes = new HashMap<String, Node>();

    public boolean connected(String from, String to) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            return false;
        }
        return nodes.get(from).connected(to);
    }

    public Integer edgeWeight(String from, String to) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            return null;
        }
        return nodes.get(from).edgeWeight(to);
    }

    public boolean connect(String from, String to, Integer weight) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            return false;
        }
        return nodes.get(from).connect(to, weight);
    }

    public boolean disconnect(String from, String to) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            return false;
        }
        return nodes.get(from).disconnect(to);
    }

    public boolean addNode(String name, Object value) {
        if (nodes.containsKey(name)) {
            return false;
        }
        Node n = new Node(name, value);
        nodes.put(name, n);
        return true;
    }

    public boolean hasNode(String name) {
        return nodes.containsKey(name);
    }

    public Object nodeValue(String name) {
        if (!nodes.containsKey(name)) {
            return null;
        }
        return nodes.get(name).payload;
    }

    public boolean changeNodeValue(String name, Object value) {
        if (!nodes.containsKey(name)) {
            return false;
        }
        nodes.get(name).changeNodeValue(value);
        return true;
    }

    public void printNode(String name) {
        if (!nodes.containsKey(name)) {
            System.out.printf("No node named %s.\n", name);
            return;
        }
        nodes.get(name).printNode();
    }

    // Runs Bellman-Ford algorithm from the given start node and writes the result to writer
    public void bellmanFord(String start, PrintWriter writer) {
        HashMap<String, Integer> distance = new HashMap<>();
        HashMap<String, String> parent = new HashMap<>();

        for (String name : nodes.keySet()) {
            distance.put(name, Integer.MAX_VALUE);
            parent.put(name, null);
        }

        distance.put(start, 0);
        parent.put(start, "0");

        int numNodes = nodes.size();

        for (int i = 0; i < numNodes - 1; i++) {
            for (String u : nodes.keySet()) {
                Node currentNode = nodes.get(u);
                for (String v : currentNode.edges.keySet()) {
                    int weight = currentNode.edges.get(v);

                    if (distance.get(u) != Integer.MAX_VALUE &&
                            distance.get(u) + weight < distance.get(v)) {

                        distance.put(v, distance.get(u) + weight);
                        parent.put(v, u);
                    }
                }
            }
        }

        writer.println(nodes.size());

        ArrayList<String> sorted = new ArrayList<>(nodes.keySet());
        Collections.sort(sorted, Comparator.comparingInt(Integer::parseInt));

        for (String name : sorted) {
            int dist = distance.get(name);
            String par = parent.get(name);
            writer.printf("%s %d %s\n", name, dist, par);
        }
    }

    // Runs Dijkstra’s algorithm from the given start node and writes the result to writer
    public void dijkstra(String start, PrintWriter writer) {
        HashMap<String, Integer> distance = new HashMap<>();
        HashMap<String, String> parent = new HashMap<>();
        HashSet<String> visited = new HashSet<>();

        for (String name : nodes.keySet()) {
            distance.put(name, Integer.MAX_VALUE);
            parent.put(name, null);
        }

        distance.put(start, 0);
        parent.put(start, "-1");

        while (visited.size() < nodes.size()) {
            String current = null;
            int minDistance = Integer.MAX_VALUE;

            for (String name : nodes.keySet()) {
                if (!visited.contains(name) && distance.get(name) < minDistance) {
                    minDistance = distance.get(name);
                    current = name;
                }
            }

            if (current == null)
                break;

            visited.add(current);
            Node currentNode = nodes.get(current);

            for (String neighbor : currentNode.edges.keySet()) {
                int weight = currentNode.edges.get(neighbor);
                int newDist = distance.get(current) + weight;

                if (newDist < distance.get(neighbor)) {
                    distance.put(neighbor, newDist);
                    parent.put(neighbor, current);
                }
            }
        }

        writer.println(nodes.size());

        ArrayList<String> sorted = new ArrayList<>(nodes.keySet());
        Collections.sort(sorted, Comparator.comparingInt(Integer::parseInt));

        for (String name : sorted) {
            int dist = distance.get(name);
            String par = parent.get(name);

            if (name.equals(start)) {
                dist = -1;
                par = "-1";
            }

            writer.printf("%s %d %s\n", name, dist, par);
        }
    }

    // Reads graph structure and start node from the input file
    public String readGraphFromFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));

        int numNodes = Integer.parseInt(reader.readLine().trim());
        String startNode = reader.readLine().trim();
        int numEdges = Integer.parseInt(reader.readLine().trim());

        for (int i = 1; i <= numNodes; i++) {
            addNode(String.valueOf(i), null);
        }

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length != 3)
                continue;

            String from = parts[0];
            String to = parts[1];
            int weight = Integer.parseInt(parts[2]);

            connect(from, to, weight);
            connect(to, from, weight);
        }

        reader.close();
        return startNode;
    }

    //Builds graph from input file and writes Dijkstra and Bellman-Ford results to output file
    public static void main(String[] args) throws Exception {
        Graph g = new Graph();
        String startNode = g.readGraphFromFile("cop3503-asn2-input.txt");

        PrintWriter writer = new PrintWriter("cop3503-asn2-output-tonanez-lucas.txt");

        writer.println("Dijkstra\n");
        g.dijkstra(startNode, writer);

        writer.println("\nBellman-Ford\n");
        g.bellmanFord(startNode, writer);

        writer.close();
    }

}