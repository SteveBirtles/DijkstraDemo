import javafx.scene.canvas.*; 
import javafx.scene.paint.*;
import java.util.Deque;
import java.util.ArrayDeque;

public class FrameRegulator
{

    private Deque<Long> frameLengths = new ArrayDeque<>();
    private Integer frameCounter = 0;
    private Long tick = 0L;
    private double frameLength = 0;

    public void updateFPS(long now, GraphicsContext gc, boolean displayFPS)
    {
        frameCounter += 1;
        Long tock = now / 1000000;
        if (tick != 0)
        {
            frameLength = (tock - tick) / 1000.0;
            frameLengths.addLast(tock - tick);
            if (frameLengths.size() > 30) frameLengths.removeFirst();
            Long total = 0L;
            for(Long x : frameLengths) total += Math.round(1000.0 / x);
            Long fps = total / frameLengths.size();
            if (displayFPS) {
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1);    
                gc.strokeText("Frame " + frameCounter.toString() + " | " + fps.toString() + " FPS", 800, 20);
            }
        }
        tick = tock;
    }

    public double getFrameLength()
    {
        return frameLength;
    }

}
