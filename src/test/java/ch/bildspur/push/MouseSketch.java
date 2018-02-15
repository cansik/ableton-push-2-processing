package ch.bildspur.push;

import ch.bildspur.push.processing.ProcessingPushContext;
import ch.bildspur.push.processing.ProcessingPushDevice;
import javafx.application.Platform;
import processing.core.PApplet;
import processing.core.PGraphics;

public class MouseSketch extends PApplet {
    public static void main(String... args) {
        MouseSketch sketch = new MouseSketch();
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
        size(960, 160, FX2D);
    }

    @Override
    public void setup()
    {
        pushContext.open();

        if(!pushContext.isPushAvailable())
            Platform.exit();

        System.out.println("Push available!");
        push = pushContext.getFirstDevice();
        push.open();

        pushGraphics = push.getPGraphics();
    }

    @Override
    public void draw() {
        background(0);

        pushGraphics.beginDraw();
        pushGraphics.background(0);

        render(pushGraphics);

        pushGraphics.text("Framecount: " + frameCount, 100, 100);
        pushGraphics.endDraw();
        push.sendFrameAsync();

        render(g);
    }

    private void render(PGraphics canvas)
    {
        canvas.rectMode(CENTER);
        canvas.ellipse(mouseX, mouseY, 50, 50);
    }

    @Override
    public void stop() {
        push.close();
    }
}
