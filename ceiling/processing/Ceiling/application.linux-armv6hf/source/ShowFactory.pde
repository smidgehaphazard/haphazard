

/**
 * Work arround Prcessing inner class silliness.
 */
public class ShowFactory {
    public final int DEFAULT_SOLID_COLOR;
    
    public ShowFactory() {
        DEFAULT_SOLID_COLOR = color(255, 231, 117);
    }

    public Show createDefault(Shows type) {
        switch (type) {
            case ANGULAR_ATTRACTOR:
                return angularAttractorShow();
            case CIRCLE_SPLATTER:
                return circleSplatterShow();
            case DOT:
                return dotShow();
            case SOLID:
                return solidShow();
            default:
                throw new IllegalStateException();
        }

    }

    public Show angularAttractorShow() {
        return new AngularAttractorShow();
    }

    public Show circleSplatterShow() {
        return new CircleSplatterShow();
    }

    public Show dotShow() {
        return new DotShow();
    }

    public Show solidShow() {
        return solidShow(DEFAULT_SOLID_COLOR);
    }

    public Show solidShow(int c) {
        return new SolidShow(c);
    }
}