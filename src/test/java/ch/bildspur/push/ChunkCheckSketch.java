package ch.bildspur.push;

import ch.bildspur.push.buffer.BufferedImageBuffer;
import ch.bildspur.push.buffer.PGraphicsBuffer;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.*;

public class ChunkCheckSketch extends PApplet {
    public static void main(String... args) {
        ChunkCheckSketch sketch = new ChunkCheckSketch();
        sketch.run();
    }

    public void run() {
        runSketch();
    }

    PGraphicsBuffer pGraphicsBuffer;
    BufferedImageBuffer bufferedImageBuffer;

    PGraphics pushGraphics;
    Graphics2D bufferedGraphics;

    @Override
    public void settings() {
        size(1024, 480, FX2D);
    }

    @Override
    public void setup() {
        pGraphicsBuffer = new PGraphicsBuffer(this);
        bufferedImageBuffer = new BufferedImageBuffer();

        pGraphicsBuffer.init();
        bufferedImageBuffer.init();

        pushGraphics = pGraphicsBuffer.getGraphics();
        bufferedGraphics = bufferedImageBuffer.getScreen().createGraphics();

        noLoop();
    }

    @Override
    public void draw() {
        background(55);

        int red = 255;
        int green = 200;
        int blue = 100;

        // draw onto buffers
        pushGraphics.beginDraw();
        pushGraphics.background(red, green, blue);
        pushGraphics.endDraw();

        bufferedGraphics.setPaint(new Color(red, green, blue));
        bufferedGraphics.fillRect(0, 0, PushConstants.DISPLAY_WIDTH, PushConstants.DISPLAY_HEIGHT);

        // prepare buffers
        pGraphicsBuffer.prepareChunk();
        bufferedImageBuffer.prepareChunk();

        // get chunk
        byte[] graphicsChunk = pGraphicsBuffer.getChunk(0);
        byte[] bufferedChunk = bufferedImageBuffer.getChunk(0);

        short[] bufferedRaw = bufferedImageBuffer.getRaw();
        int[] pgraphicsRaw = pGraphicsBuffer.getRaw();

        // get red part
        IntColor graphicsPixel = new IntColor(
                (pgraphicsRaw[0] & PImage.RED_MASK) >> 16,
                (pgraphicsRaw[0] & PImage.GREEN_MASK) >> 8,
                pgraphicsRaw[0] & PImage.BLUE_MASK);

        IntColor bufferedPixel = new IntColor(
                bufferedRaw[0] & 0x001f,
                (bufferedRaw[0] & 0x07e0) >> 6,
                (bufferedRaw[0] & 0xf800) >> 11);

        // draw chunks onto screen
        fill(255);
        textSize(20);

        String bufferedString = getByteString(bufferedChunk);
        String graphicsString = getByteString(graphicsChunk);

        text(bufferedString, 30, 40);
        text(graphicsString, 30, 80);

        System.out.println("");
        System.out.println("Buffered P0: " + bufferedPixel);
        System.out.println("Graphics P0: " + graphicsPixel);

        System.out.println("");
        System.out.println(getShortString(bufferedRaw).substring(0, 200));
        System.out.println(getIntString(pgraphicsRaw).substring(0, 200));


        System.out.println("");
        System.out.println(bufferedString.substring(0, 200));
        System.out.println(graphicsString.substring(0, 200));

        System.out.println("");
        System.out.println("Buffered: " + bufferedChunk.length);
        System.out.println("Graphics: " + graphicsChunk.length);

        System.out.println("");
        // compare
        boolean isDifferent = false;
        int i;
        for (i = 0; i < bufferedChunk.length; i++) {
            if (graphicsChunk[i] != bufferedChunk[i]) {
                isDifferent = true;
                break;
            }
        }

        if (isDifferent) {
            System.out.println("Is different at: " + i);
        } else {
            System.out.println("Is same!");
        }

        exit();
    }

    private String getByteString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    private String getIntString(int[] data) {
        StringBuilder sb = new StringBuilder();
        for (int b : data) {
            sb.append(String.format("%S ", b));
        }
        return sb.toString();
    }

    private String getShortString(short[] data) {
        StringBuilder sb = new StringBuilder();
        for (short b : data) {
            sb.append(String.format("%S ", b));
        }
        return sb.toString();
    }

    @Override
    public void stop() {
    }

    private class IntColor {
        int red;
        int blue;
        int green;

        public IntColor(int red, int green, int blue) {
            this.red = red;
            this.blue = blue;
            this.green = green;
        }

        @Override
        public String toString() {
            return "[R: " + red + " G: " + green + " B: " + blue + "]";
        }
    }
}
