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

    PushDevice push = new PushDevice(this);

    @Override
    public void settings()
    {
        size(500, 500, FX2D);
    }

    @Override
    public void setup()
    {
        if(!push.open())
        {
            println("push device could not be opened!");
            exit();
        }
    }

    @Override
    public void draw() {
        background(100, 200, 50);
    }
}
