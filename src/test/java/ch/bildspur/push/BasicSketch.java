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

    @Override
    public void settings()
    {
        size(500, 500);
    }

    @Override
    public void setup()
    {

    }

    @Override
    public void draw() {
        background(100, 200, 50);
    }
}
