import java.util.HashMap;

public class Node
{

    public static double maxX;
    public static double maxY;
    public static double minX;
    public static double minY;   

    public double value;
    public double heuristicValue;
    public boolean checked;

    public double x;
    public double y;
    public double dx;
    public double dy;    

    public HashMap<Node, Boolean> edges;

    public Node(double x, double y, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        edges = new HashMap<Node, Boolean>();
        this.value = Double.MAX_VALUE;     
        this.checked = false;
        this.heuristicValue = 0;
    }

    public void update(double framelength) {
        x += dx * framelength;
        y += dy * framelength;

        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
        if (x < minX) minX = x;
        if (y < minY) minY = y;

        if (Application.BOUNCE) {
            if ((x < -Application.WINDOW_WIDTH/2 + Application.NODE_RADIUS) && dx < 0) dx = -dx;
            if ((y < -Application.WINDOW_HEIGHT/2 + Application.NODE_RADIUS) && dy < 0) dy = -dy;
            if ((x > Application.WINDOW_WIDTH/2 - Application.NODE_RADIUS) && dx > 0) dx = -dx;
            if ((y > Application.WINDOW_HEIGHT/2 - Application.NODE_RADIUS) && dy > 0) dy = -dy;
        }
        else if (Application.WRAP) {
            if (x < -Application.WINDOW_WIDTH/2 - Application.NODE_RADIUS) x += Application.WINDOW_WIDTH + Application.NODE_RADIUS*2;
            if (y < -Application.WINDOW_HEIGHT/2 - Application.NODE_RADIUS) y += Application.WINDOW_HEIGHT + Application.NODE_RADIUS*2;
            if (x > Application.WINDOW_WIDTH/2 + Application.NODE_RADIUS) x -= Application.WINDOW_WIDTH + Application.NODE_RADIUS*2;
            if (y > Application.WINDOW_HEIGHT/2 + Application.NODE_RADIUS) y -= Application.WINDOW_HEIGHT + Application.NODE_RADIUS*2;
        }
    }   

    public void addEdge(Node node) {
        edges.put(node, true);
        node.edges.put(this, false);
    }

    public static double distance(Node node1, Node node2) {
        return (Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2)));
    }

}
