package ch.bildspur.push;

import ch.bildspur.push.midi.PushMidi;
import ch.bildspur.push.processing.ProcessingPushContext;
import ch.bildspur.push.processing.ProcessingPushDevice;
import javafx.application.Platform;
import processing.core.PApplet;
import processing.core.PGraphics;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;

import javax.sound.midi.MidiUnavailableException;

public class MidiSketch extends PApplet {
    public static void main(String... args) {
        MidiSketch sketch = new MidiSketch();
        sketch.run();
    }

    public void run()
    {
        runSketch();
    }

    ProcessingPushContext pushContext = new ProcessingPushContext(this);
    ProcessingPushDevice push;
    PGraphics pushGraphics;

    PushMidi midi;

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

        try {
            doMidiStuff();
        } catch (CoreMidiException | MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw() {
        background(100, 200, 50);

        pushGraphics.beginDraw();
        pushGraphics.background(255, 0, 0);
        pushGraphics.text("Framecount: " + frameCount, 100, 100);
        pushGraphics.endDraw();

        push.sendFrameAsync();
    }

    void doMidiStuff() throws CoreMidiException, MidiUnavailableException {
        midi = new PushMidi();
        midi.getPushDevices();
        midi.open();
    }

    @Override
    public void stop() {
        push.close();
    }
}
