import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

import java.util.ArrayList;
import java.util.List;

public class Network {
    private static final int nrOfTests = 1000000;
    private final static double TMax = 0.05;
    private static final int avgPacketSize = 1500 * 8; //[b/p - bits per packet] - 12 000
    private static final int[][] N = new int[20][20]; // macierz natężęń
    private static int sumN = 0;

    private static final Graph<Integer, NetworkEdge> graph = new SimpleGraph<>(NetworkEdge.class);

    private static int overflowCnt;
    private static int TMaxCnt;
    private static int notConnectedCnt;


    static void initGraph() {
        // V
        for (int i = 1; i <= 20; i++) {
            Network.graph.addVertex(i);
        }

        // E
        for (int i = 1; i < 10; i++) {
            Network.graph.addEdge(i, i + 1);
            if (i != 5) Network.graph.addEdge(i, i + 10);
        }
        for (int i = 11; i < 20; i++) {
            Network.graph.addEdge(i, i + 1);
        }
        Network.graph.addEdge(10, 1);
        Network.graph.addEdge(20, 11);
        Network.graph.addEdge(15, 20);
    }

    static void initN() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (i == j) N[i][j] = 0;
                else {
                    N[i][j] = (int) (Math.random() * 15); // od 0 do 14
                    sumN += N[i][j];
                }
            }
        }
    }

    static int initA(Graph<Integer, NetworkEdge> g, boolean adjustC) {
        for (NetworkEdge edge : g.edgeSet()) edge.resetA();
        DijkstraShortestPath<Integer, NetworkEdge> pathG = new DijkstraShortestPath<>(g);
        for (int i = 0; i < 20; i++) {
            ShortestPathAlgorithm.SingleSourcePaths<Integer, NetworkEdge> paths = pathG.getPaths(i + 1);
            for (int j = 0; j < 20; j++) {
                List<NetworkEdge> edges = paths.getPath(j + 1).getEdgeList();
                for (NetworkEdge edge : edges) {
                    edge.a += N[i][j];
                    if (edge.a >= edge.c / avgPacketSize) {
                        if (adjustC) {
                            edge.c = 100000000;
                            if (edge.a >= edge.c / avgPacketSize)
                                edge.c = 1000000000;
                            else {
                                return -1;
                            }
                        } else return -1;
                    }
                }
            }
        }
        return 0;
    }

    static double estimateAvailability(int sampleSize) { // z dokładnością do 1/sqrt(sampleSize)
        int good = 0;
        for (int k = 0; k < sampleSize; k++) {
            if (k % 10000 == 0) System.out.println(k + "/" + sampleSize);
            Graph<Integer, NetworkEdge> graphCopy = new SimpleGraph<>(NetworkEdge.class);
            Graphs.addGraph(graphCopy, graph);
            ConnectivityInspector<Integer, NetworkEdge> cnctGraphCopy
                    = new ConnectivityInspector<>(graphCopy);
            // deleting edges
            ArrayList<NetworkEdge> edgeList = new ArrayList<>(graphCopy.edgeSet());
            for (NetworkEdge edge : edgeList) {
                if (Math.random() > edge.p) {
                    graphCopy.removeEdge(edge);
                }
            }
            // is connected?
            if (!cnctGraphCopy.isGraphConnected()) {
                notConnectedCnt++;
                continue;
            }
            if (initA(graphCopy, false) == -1) {
                overflowCnt++;
                continue;
            }

            // Tśr < Tmax?
            double T = 0;
            for (NetworkEdge edge : graphCopy.edgeSet()) {
                T += (edge.a / (((double) edge.c / avgPacketSize) - edge.a));
            }
            T *= (1f / sumN);
            if (T < TMax) good++;
            else
                TMaxCnt++;
        }
        return (double) good / sampleSize;
    }

    public static void main(String[] args) {

        initGraph(); // different topologies: initGraph20e(), initGraph24e(), initGraph30e(), initGraph30ev2(),
        initN();
        if (initA(graph, true) == -1) {
            System.out.println("Error while generating graph.");
            System.exit(-1);
        }

        //for(NetworkEdge edge : graph.edgeSet()) edge.c *= 1.5; // dla testów z większą przepustowością

        TMaxCnt = 0;
        overflowCnt = 0;
        notConnectedCnt = 0;

        System.out.println("Niezawodnosc = " + estimateAvailability(nrOfTests));
        System.out.println("Liczba testow: " + nrOfTests);
        System.out.println("Ile razy co spowodowalo usterke:");
        System.out.println("\tGraph not connected: " + notConnectedCnt);
        System.out.println("\tEdge overflow: " + overflowCnt);
        System.out.println("\tT > Tmax: " + TMaxCnt);
        System.out.println("[Tmax = " + TMax + ", m = " + avgPacketSize + ", Nsr = " + sumN / 380.0 + "]");

    }

    static void initGraph20e() {
        // V
        for (int i = 1; i <= 20; i++) {
            Network.graph.addVertex(i);
        }

        // E
        for (int i = 1; i <= 20; i++) {
            Network.graph.addEdge(i, i % 20 + 1);
        }
    }

    static void initGraph24e() {
        // V
        for (int i = 1; i <= 20; i++) {
            Network.graph.addVertex(i);
        }

        // E
        for (int i = 1; i <= 20; i++) {
            Network.graph.addEdge(i, i % 20 + 1);
        }
        graph.addEdge(20, 17);
        graph.addEdge(2, 15);
        graph.addEdge(4, 13);
        graph.addEdge(6, 11);
    }

    static void initGraph30e() {
        // V
        for (int i = 1; i <= 20; i++) {
            Network.graph.addVertex(i);
        }

        // E
        for (int i = 1; i <= 10; i++) {
            Network.graph.addEdge(i, i % 10 + 1);
            Network.graph.addEdge(i, i + 10);
        }
        for (int i = 11; i <= 20; i++) {
            Network.graph.addEdge(i, i % 10 + 11);
        }
    }

    static void initGraph30ev2() {
        // V
        for (int i = 1; i <= 20; i++) {
            Network.graph.addVertex(i);
        }

        // E
        for (int i = 1; i <= 10; i++) {
            Network.graph.addEdge(i, i % 10 + 1);
            if (i != 5 && i != 10) Network.graph.addEdge(i, i + 10);
        }
        for (int i = 11; i <= 20; i++) {
            Network.graph.addEdge(i, i % 10 + 11);
        }
        graph.addEdge(15, 20);
        graph.addEdge(5, 10);
    }

}
