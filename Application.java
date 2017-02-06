import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.scene.canvas.*; 
import javafx.scene.effect.*;
import javafx.animation.AnimationTimer; 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;

public class Application {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 1024;

    public static final double NODE_RADIUS = 15;
    public static final int NODE_COUNT = 50;
    public static final int JOIN_COUNT = 5;

    public static final boolean BOUNCE = true;
    public static final boolean WRAP = false;
    public static final boolean SCALE = false;

    static HashSet<KeyCode> keysPressed = new HashSet<>();

    public static void main(String args[]) {       
        JFXPanel panel = new JFXPanel();        
        Platform.runLater(() -> start());               
    }

    private static void start() {

        System.out.println("Application Starting...");

        FrameRegulator fr = new FrameRegulator();
        Random rnd = new Random();

        Group root = new Group();
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        Canvas canvas = new Canvas();

        stage.setTitle("JavaFX Canvas Demo");
        stage.setResizable(false);
        stage.setFullScreen(true);
        stage.setScene(scene);                        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we) {
                    System.out.println("Close button was clicked!");
                    Application.terminate();
                }
            });
        stage.show(); 
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);        

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> keysPressed.add(event.getCode()));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> keysPressed.remove(event.getCode()));

        canvas.setWidth(WINDOW_WIDTH);
        canvas.setHeight(WINDOW_HEIGHT);            
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.WHITE);
        gc.setFont(new Font("Arial", 14));       

        Node[] nodeArray = new Node[NODE_COUNT];
        ArrayList<Node> nodeList = new ArrayList<>();
        for (int i = 0; i < nodeArray.length; i++) {
            nodeArray[i] = new Node(rnd.nextDouble()*WINDOW_WIDTH - WINDOW_WIDTH/2, rnd.nextDouble()*WINDOW_HEIGHT - WINDOW_HEIGHT/2, rnd.nextDouble()*100-50, rnd.nextDouble()*100-50);
            nodeList.add(nodeArray[i]);                             
        }

        new AnimationTimer() {
            @Override
            public void handle(long now) {

                double scale;
                if (SCALE) scale = 1/Math.sqrt(Math.pow((Node.maxX - Node.minX)/1280,2) + Math.pow((Node.maxY - Node.minY)/1280,2));                
                else scale = 1;

                /* INPUT */

                for(KeyCode k : keysPressed) {
                    if (k == KeyCode.ESCAPE) Application.terminate();

                    if (k == KeyCode.SPACE) {
                        nodeList.clear();
                        for (int i = 0; i < nodeArray.length; i++) {
                            nodeArray[i] = new Node(rnd.nextDouble()*WINDOW_WIDTH - WINDOW_WIDTH/2, rnd.nextDouble()*WINDOW_HEIGHT - WINDOW_HEIGHT/2, 0, 0);
                            nodeList.add(nodeArray[i]);                             
                        }
                    }

                    if (k == KeyCode.ENTER) {
                        for (int i = 0; i < nodeArray.length; i++) {
                            nodeArray[i].dx = rnd.nextDouble()*100-50;
                            nodeArray[i].dy = rnd.nextDouble()*100-50;                            
                        }
                    }
                }

                /* PROCESS */

                for (Node node : nodeList) {
                    node.update(fr.getFrameLength());
                    node.edges.clear();
                }

                ArrayList<Double> distances = new ArrayList<>();                
                for (Node node : nodeList) {
                    distances.clear();
                    for (Node other : nodeList) {
                        if (node == other) continue;
                        distances.add(Node.distance(node, other));
                    }
                    Collections.sort(distances);
                    for (Node other : nodeList) {
                        if (Node.distance(node, other) < distances.get(JOIN_COUNT)) {
                            node.addEdge(other);
                        }
                    }
                }

                for (Node node : nodeList) {
                    node.checked = false;
                    node.value = Double.MAX_VALUE;
                }
                nodeArray[0].value = 0;

                Node node = nodeArray[0];
                do {
                    for (Node child : node.edges.keySet()) {
                        if (!child.checked) {
                            double d = node.value + Node.distance(node, child);
                            if (d < child.value) {
                                child.value = d;                                        
                            }
                        }
                    }
                    node.checked = true;
                    node = null;
                    double lowestValue = Double.MAX_VALUE;
                    for (Node nextNode : nodeList) {                        
                        if (!nextNode.checked && nextNode.value < lowestValue) {
                            node = nextNode;
                            lowestValue = nextNode.value;
                        }
                    }    

                } while (node != null);

                HashMap<Node, Node> path = new HashMap<>();
                node = nodeArray[nodeArray.length - 1];
                int steps = 0;
                do {                             
                    Node last = node;
                    double bestValue = Double.MAX_VALUE;
                    for (Node parent : node.edges.keySet()) {
                        if (parent.value < bestValue) {
                            bestValue = parent.value;
                            node = parent;
                        }
                    }
                    path.put(node, last);
                    steps++;
                } while (steps < nodeArray.length && node != nodeArray[0]);

                /* OUTPUT */

                double centreX = WINDOW_WIDTH * scale / 2;
                double centreY = WINDOW_HEIGHT * scale / 2;

                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

                for (Node n : nodeList) {
                    for (Node child : n.edges.keySet()) {
                        if (!n.edges.get(child)) continue;
                        double d = Node.distance(n, child) / 1280;
                        if (d > 1 || d == 0) continue;
                        gc.setStroke(Color.color(0.5, 0.5, 0.5, 1 - d));
                        gc.setLineWidth(5);
                        gc.strokeLine(n.x*scale + centreX, n.y*scale + centreY, child.x*scale + centreX, child.y*scale + centreY);    
                        gc.setLineWidth(1);
                        gc.strokeText(Integer.toString((int) (d * 1280)), (n.x + child.x)*scale / 2 + centreX, (n.y + child.y)*scale / 2 + centreY);
                    }
                }                               

                for (Node n : path.keySet()) {
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(5);
                    gc.strokeLine(n.x*scale + centreX, n.y*scale + centreY, path.get(n).x*scale + centreX, path.get(n).y*scale + centreY);  
                }

                for (Node n : nodeList) {
                    if (n == nodeArray[0]) gc.setFill(Color.LIME);
                    else if (n == nodeArray[nodeArray.length - 1]) gc.setFill(Color.RED);   
                    else if (path.containsKey(n)) gc.setFill(Color.YELLOW);
                    else gc.setFill(Color.BLUE);
                    gc.fillOval(n.x*scale-NODE_RADIUS*scale + centreX, n.y*scale-NODE_RADIUS*scale + centreY, NODE_RADIUS*2*scale, NODE_RADIUS*2*scale);
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(1);
                    if (n.value != Double.MAX_VALUE) gc.strokeText(Integer.toString((int) n.value), n.x*scale + centreX, n.y*scale + centreY);
                    else gc.strokeText("∞", n.x*scale + centreX, n.y*scale + centreY);                    
                }    

                fr.updateFPS(now, gc, false);
            }
        }.
        start();

    }

    public static void terminate() {
        System.out.println("Terminating Application...");
        System.exit(0);
    }

}
