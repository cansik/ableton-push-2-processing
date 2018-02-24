package ch.bildspur.push.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class PushReceiver implements Receiver {

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if(message instanceof ShortMessage)
        {
            ShortMessage msg = (ShortMessage)message;
            System.out.println("Midi => Channel: " + msg.getChannel()
                    + " Command: " + msg.getCommand()
                    + " Data1: " + msg.getData1()
                    + " Data2: " + msg.getData2());
        }
    }

    @Override
    public void close() {

    }
}
