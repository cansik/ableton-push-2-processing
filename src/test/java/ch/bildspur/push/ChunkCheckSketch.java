package ch.bildspur.push;

import ch.bildspur.push.buffer.BufferedImageBuffer;
import ch.bildspur.push.buffer.PGraphicsBuffer;
import processing.core.PApplet;
import processing.core.PGraphics;

import java.awt.*;
import java.util.Arrays;

public class ChunkCheckSketch extends PApplet {
    public static void main(String... args) {
        ChunkCheckSketch sketch = new ChunkCheckSketch();
        sketch.run();
    }

    public void run()
    {
        runSketch();
    }

    PGraphicsBuffer pGraphicsBuffer;
    BufferedImageBuffer bufferedImageBuffer;

    PGraphics pushGraphics;
    Graphics2D bufferedGraphics;

    @Override
    public void settings()
    {
        size(1024, 480, FX2D);
    }

    @Override
    public void setup()
    {
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

        // draw onto buffers
        pushGraphics.beginDraw();
        pushGraphics.background(0);
        pushGraphics.endDraw();

        bufferedGraphics.setPaint(new Color(0, 0, 0));
        bufferedGraphics.fillRect(0, 0, PushConstants.DISPLAY_WIDTH, PushConstants.DISPLAY_HEIGHT);

        // prepare buffers
        pGraphicsBuffer.prepareChunk();
        bufferedImageBuffer.prepareChunk();

        // get chunk
        byte[] graphicsChunk = pGraphicsBuffer.getChunk(0);
        byte[] bufferedChunk = bufferedImageBuffer.getChunk(0);

        // draw chunks onto screen
        fill(255);
        textSize(20);

        String bufferedString = getByteString(bufferedChunk);
        String graphicsString = getByteString(graphicsChunk);

        text(bufferedString, 30, 40);
        text(graphicsString, 30, 80);


        System.out.println(bufferedString.substring(0, 200));
        System.out.println(graphicsString.substring(0, 200));

        System.out.println("");
        System.out.println("Buffered: " + bufferedChunk.length);
        System.out.println("Graphics: " + graphicsChunk.length);

        System.out.println("");
        // compare
        boolean isDifferent = false;
        int i;
        for(i = 0; i < bufferedChunk.length; i++)
        {
            if(graphicsChunk[i] != bufferedChunk[i]) {
                isDifferent = true;
                break;
            }
        }

        if(isDifferent)
        {
            System.out.println("Is different at: " + i);
        }
        else {
            System.out.println("Is same!");
        }
    }

    private String getByteString(byte[] data)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    @Override
    public void stop() {
    }
}
