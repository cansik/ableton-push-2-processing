package ch.bildspur.push.buffer;

import ch.bildspur.push.PushConstants;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class PGraphicsBuffer implements PushImageBuffer, PushConstants {
    private PGraphics graphics;
    private PApplet parent;

    public PGraphicsBuffer(PApplet parent)
    {
        this.parent = parent;
    }

    @Override
    public void init() {
        graphics = parent.createGraphics(DISPLAY_WIDTH, DISPLAY_HEIGHT, PConstants.JAVA2D);
    }

    @Override
    public byte[] getChunk(int index) {
        //todo: Implement get chunk
        return new byte[LINES_PER_TRANSFER];
    }

    @Override
    public void dispose() {
        graphics.dispose();
    }

    public PGraphics getGraphics() {
        return graphics;
    }
}
