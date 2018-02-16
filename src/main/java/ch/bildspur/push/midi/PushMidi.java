package ch.bildspur.push.midi;

import uk.co.xfactorylibrarians.coremidi4j.*;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PushMidi implements PushMidiConstants {
    private CoreMidiDeviceProvider midi;
    private MidiDevice push;

    private boolean isOpen;

    public PushMidi() throws CoreMidiException {
        midi = new CoreMidiDeviceProvider();
    }

    public boolean open() throws MidiUnavailableException {
        if (isOpen)
            return false;

        // look for device
        Optional<CoreMidiDeviceInfo> endPointInfo = getPushDevices().stream()
                .filter(e -> e.getEndPointName().equals(ABLETON_PUSH_MIDI_ENDPOINT))
                .findFirst();

        // if no device found
        if (!endPointInfo.isPresent())
            return false;

        // setup midi device
        push = midi.getDevice(endPointInfo.get());
        push.open();

        push.getTransmitter().setReceiver(new PushReceiver());

        isOpen = true;
        return true;
    }

    public void close() {
        if (!isOpen)
            return;

        // cleanup
        push.close();
        midi = null;

        isOpen = false;
    }

    public List<CoreMidiDeviceInfo> getPushDevices() {
        return Arrays.stream(midi.getDeviceInfo())
                .filter(e -> e instanceof CoreMidiDeviceInfo)
                .map(e -> (CoreMidiDeviceInfo) e)
                .filter(e -> e.getDeviceName().equals(ABLETON_PUSH_MIDI_DEVICE_NAME)
                        && e.getVendor().equals(ABLETON_PUSH_MIDI_VENDOR))
                .collect(Collectors.toList());
    }
}
