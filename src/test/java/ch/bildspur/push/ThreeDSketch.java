package ch.bildspur.push;

import ch.bildspur.push.processing.ProcessingPushContext;
import ch.bildspur.push.processing.ProcessingPushDevice;
import javafx.application.Platform;
import processing.core.PApplet;
import processing.core.PGraphics;

public class ThreeDSketch extends PApplet {
    public static void main(String... args) {
        ThreeDSketch sketch = new ThreeDSketch();
        sketch.run();
    }

    public void run() {
        runSketch();
    }

    ProcessingPushContext pushContext = new ProcessingPushContext(this);
    ProcessingPushDevice push;
    PGraphics pushGraphics;

    PGraphics canvas;

    @Override
    public void settings() {
        size(960, 160, P2D);
    }

    @Override
    public void setup() {
        pushContext.open();

        if (!pushContext.isPushAvailable()) {
            Platform.exit();
        }

        System.out.println("Push available!");
        push = pushContext.getFirstDevice();
        push.open();

        canvas = createGraphics(width, height, P3D);
        pushGraphics = push.getPGraphics();
    }

    @Override
    public void draw() {
        background(100, 200, 50);

        canvas.beginDraw();
        canvas.background(55);

        canvas.pushMatrix();

        canvas.translate(width / 2, height / 2);
        canvas.rotateX(radians(frameCount % 360));
        canvas.rotateZ(radians(frameCount % 360));

        canvas.noStroke();
        canvas.fill(20, 20, 20);
        canvas.box(100);

        canvas.fill(150, 255, 255);
        canvas.sphere(60);

        canvas.popMatrix();
        canvas.endDraw();

        // laod texture pixels
        canvas.loadPixels();

        // send image to push
        pushGraphics.beginDraw();
        pushGraphics.background(0);

        pushGraphics.image(canvas, 0, 0);

        pushGraphics.fill(255);
        pushGraphics.textSize(18);
        pushGraphics.text("FPS: " + frameRate, 20, 30);

        pushGraphics.endDraw();

        push.sendFrameAsync();

        image(canvas, 0, 0, width, height);

        fill(255);
        textSize(18);
        text("FPS: " + frameRate, 20, 30);
    }

    @Override
    public void stop() {
        push.close();
    }
}
