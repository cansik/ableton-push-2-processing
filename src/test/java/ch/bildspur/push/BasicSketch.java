package ch.bildspur.push;

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

    @Override
    public void settings()
    {
        size(500, 500, FX2D);
    }

    @Override
    public void setup()
    {
        pushContext.open();
        System.out.println("Push available: " + pushContext.isPushAvailable());
    }

    @Override
    public void draw() {
        background(100, 200, 50);
    }
}
