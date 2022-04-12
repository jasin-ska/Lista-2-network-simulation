import org.jgrapht.graph.DefaultEdge;

public class NetworkEdge extends DefaultEdge {
    public double p; // probability of not breaking down
    public int a; // np 1000 [pakietow]
    public int c; // 10 megabit/100 megabit/1 gigabit - klasyk 10 000 000 b/s

    public NetworkEdge() {
        c = 10000000;
        p = 0.99f;
        a = 0;
    }

    public void resetA() {
        a = 0;
    }
}
