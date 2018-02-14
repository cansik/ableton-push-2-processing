package ch.bildspur.push;

import javafx.application.Platform;
import processing.core.PApplet;

public class BasicSketch extends PApplet {
    public static void main(String... args) {
        BasicSketch sketch = new BasicSketch();
        sketch.run();
    }

    public void run()
    {
        runSketch();
    }

    PushContext pushContext = new PushContext(this);
    PushDevice push;

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
    }

    @Override
    public void draw() {
        background(100, 200, 50);

        push.sendFrameAsync();
    }

    @Override
    public void stop() {
        push.close();
    }
}
