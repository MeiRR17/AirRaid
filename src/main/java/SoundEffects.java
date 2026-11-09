import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Optional original synthesized sound effects. No copyrighted sample files or
 * network services are required. Audio stays OFF by default; no mixer is
 * opened until a user deliberately enables the toolbar Sound button.
 */
public final class SoundEffects implements AutoCloseable {
    public enum Cue { SHOT, HIT, MISS, ROUND_END }

    static final int SAMPLE_RATE = 22_050;
    private final Map<Cue, Clip> clips = new EnumMap<>(Cue.class);
    private boolean enabled;

    public boolean isEnabled() { return enabled; }

    /**
     * Returns false rather than crashing if the host has no playable audio
     * device (common on headless CI, remote desktops and air-gapped hosts).
     */
    public boolean setEnabled(boolean requested) {
        if (!requested) {
            enabled = false;
            for (Clip clip : clips.values()) clip.stop();
            return true;
        }
        if (enabled) return true;
        try {
            if (clips.isEmpty()) {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
                for (Cue cue : Cue.values()) {
                    Clip clip = AudioSystem.getClip();
                    byte[] pcm = synthesize(cue);
                    try {
                        clip.open(format, pcm, 0, pcm.length);
                    } catch (LineUnavailableException | RuntimeException failed) {
                        clip.close();
                        throw failed;
                    }
                    clips.put(cue, clip);
                }
            }
            enabled = true;
            return true;
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            enabled = false;
            close();
            return false;
        }
    }

    public void play(Cue cue) {
        if (!enabled) return;
        Clip clip = clips.get(cue);
        if (clip == null) return;
        try {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch (IllegalStateException ignored) {
            // Device disconnects must not affect gameplay or crash the EDT.
            enabled = false;
            close();
        }
    }

    /**
     * Generates a short, enveloped 16-bit mono cue deterministically.
     * All tones are mathematical waveforms authored for AirRaid.
     */
    static byte[] synthesize(Cue cue) {
        if (cue == null) throw new IllegalArgumentException("cue required");
        int durationMs = switch (cue) {
            case SHOT -> 100;
            case HIT -> 160;
            case MISS -> 170;
            case ROUND_END -> 300;
        };
        int frames = SAMPLE_RATE * durationMs / 1000;
        byte[] pcm = new byte[frames * 2];
        double phase = 0;
        for (int i = 0; i < frames; i++) {
            double t = i / (double) SAMPLE_RATE;
            double progress = i / (double) Math.max(1, frames - 1);
            double hz = switch (cue) {
                case SHOT -> 720 - 360 * progress;
                case HIT -> 420 + 560 * progress;
                case MISS -> 260 - 150 * progress;
                case ROUND_END -> 400 + (int) (progress * 3) * 130;
            };
            phase += 2 * Math.PI * hz / SAMPLE_RATE;
            // Quiet envelope: soft attack and a smooth release to zero.
            double attack = Math.min(1.0, progress * 35);
            double release = Math.pow(1.0 - progress, 1.7);
            double sample = Math.sin(phase) * attack * release * 0.25;
            int signed = (int) Math.round(sample * Short.MAX_VALUE);
            pcm[i * 2] = (byte) signed;
            pcm[i * 2 + 1] = (byte) (signed >>> 8);
        }
        return pcm;
    }

    @Override public void close() {
        enabled = false;
        for (Clip clip : clips.values()) {
            clip.stop();
            clip.close();
        }
        clips.clear();
    }
}
