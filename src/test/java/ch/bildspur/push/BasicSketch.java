package ch.bildspur.push;

import ch.bildspur.push.buffer.BufferedImageBuffer;
import javafx.application.Platform;
import processing.core.PApplet;

import java.awt.*;

public class BasicSketch extends PApplet {
    public static void main(String... args) {
        BasicSketch sketch = new BasicSketch();
        sketch.run();
    }

    public void run()
    {
        runSketch();
    }

    PushContext pushContext = new PushContext();
    PushDevice push;
    BufferedImageBuffer screenBuffer;

    Graphics2D pushGraphics;

    @Override
    public void settings()
    {
        size(500, 500, FX2D);
    }

    @Override
    public void setup()
    {
        pushContext.open();

        if(!pushContext.isPushAvailable())
        {
            Platform.exit();
        }

        System.out.println("Push available!");
        screenBuffer = new BufferedImageBuffer();
        push = pushContext.getFirstDevice();
        push.open(screenBuffer);

        pushGraphics = screenBuffer.getScreen().createGraphics();
    }

    @Override
    public void draw() {
        background(100, 200, 50);

        pushGraphics.setPaint(new Color(255, 0, 0));
        pushGraphics.fillRect(0, 0, PushConstants.DISPLAY_WIDTH, PushConstants.DISPLAY_HEIGHT);
        pushGraphics.setPaint(new Color(255, 255, 255));
        pushGraphics.drawString("Framecount: " + frameCount, 100, 100);

        push.sendFrameAsync();
    }

    @Override
    public void stop() {
        push.close();
    }
}
