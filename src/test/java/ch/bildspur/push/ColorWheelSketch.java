package ch.bildspur.push;

import ch.bildspur.push.processing.ProcessingPushContext;
import ch.bildspur.push.processing.ProcessingPushDevice;
import javafx.application.Platform;
import processing.core.PApplet;
import processing.core.PGraphics;

public class ColorWheelSketch extends PApplet {
    public static void main(String... args) {
        ColorWheelSketch sketch = new ColorWheelSketch();
        sketch.run();
    }

    public void run()
    {
        runSketch();
    }

    ProcessingPushContext pushContext = new ProcessingPushContext(this);
    ProcessingPushDevice push;
    PGraphics pushGraphics;
    int r = 0;
    int g = 0;
    int b = 0;

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
        background(r, g, b);

        pushGraphics.beginDraw();
        pushGraphics.background(r, g, b);
        pushGraphics.fill((r + 128) % 255, (g + 128) % 255, (b + 128) % 255);
        pushGraphics.text("Framecount: " + frameCount, 100, 100);
        pushGraphics.endDraw();

        r = (r + 1) % 255;

        if(frameCount % 2 == 0)
            g = (g + 1) % 255;

        if(frameCount % 3 == 0)
        b = (b + 1) % 255;

        push.sendFrameAsync();
    }

    @Override
    public void stop() {
        push.close();
    }
}
