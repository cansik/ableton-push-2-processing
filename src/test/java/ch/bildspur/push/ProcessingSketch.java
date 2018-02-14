package ch.bildspur.push;

import ch.bildspur.push.buffer.BufferedImageBuffer;
import ch.bildspur.push.buffer.PGraphicsBuffer;
import ch.bildspur.push.processing.ProcessingPushContext;
import ch.bildspur.push.processing.ProcessingPushDevice;
import javafx.application.Platform;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.awt.*;

public class ProcessingSketch extends PApplet {
    public static void main(String... args) {
        ProcessingSketch sketch = new ProcessingSketch();
        sketch.run();
    }

    public void run()
    {
        runSketch();
    }

    ProcessingPushContext pushContext = new ProcessingPushContext(this);
    ProcessingPushDevice push;
    PGraphics pushGraphics;

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
        push = pushContext.getFirstDevice();
        push.open();

        pushGraphics = push.getPGraphics();
    }

    @Override
    public void draw() {
        background(100, 200, 50);

        pushGraphics.beginDraw();
        pushGraphics.background(0);
        pushGraphics.text("Framecount: " + frameCount, 100, 100);
        pushGraphics.endDraw();

        push.sendFrameAsync();
    }

    @Override
    public void stop() {
        push.close();
    }
}
