import java.util.*;
import java.time.Duration;
import java.time.Instant;

/**
 * A Ceiling meta-show that loops through a predefined set of shows.
 * 
 * - Loops on a timer and on a keypress
 */
public class ShowLoopController {
    private final Shows[] loopable = {
        //Shows.ANGULAR_ATTRACTOR,
        Shows.CIRCLE_SPLATTER,


        // Shows.DOT,
        // Shows.SOLID,
    };

    private final ShowFactory showFactory;
    private final Duration loopDuration;

    private Instant nextShowDeadline;
    private int currentShowIdx;
    private Show currentShow;

    public ShowLoopController(ShowFactory showFactory, Duration loopDuration) {
        this.showFactory = showFactory;
        this.loopDuration = loopDuration;

        nextShow();
    }

    public Show currentShow() {
        if (deadlineExceeded()) {
            nextShow();
        }

        return this.currentShow;
    }

    public void nextShow() {
        resetDeadline();
        currentShowIdx = (currentShowIdx + 1) % loopable.length;
        currentShow = showFactory.createDefault(loopable[currentShowIdx]);
    }

    private boolean deadlineExceeded() {
        return Instant.now().isAfter(nextShowDeadline);
    }

    private void resetDeadline() {
        nextShowDeadline = Instant.now().plus(loopDuration);
    }

}