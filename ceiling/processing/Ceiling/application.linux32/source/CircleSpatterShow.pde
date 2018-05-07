/**
 * Adapted from https://www.openprocessing.org/sketch/157175
 */
public class CircleSplatterShow extends Show {
    List<Circle> circles = new ArrayList<Circle>();

    int min = 100;
    int max = 200;

    Circle c;
    
    public CircleSplatterShow() {
      c  = new Circle(new PVector(0, 0), 20, randomColor());
      circles.add(c);
    }

    void draw() {        
        garbageCollect();
        attemptAdd();
        
        background(0);
        for (Circle c: circles) {
            c.draw();
        }
    }
    
    int randomColor() {
        // Purples
        return color(random(255), 0, random(75, 255));

        // Cool blue greens
        //return color(0, random(255), random(75, 255));

        // Muted teals
        //return color(100, 150, random(150,255));
    }

    void garbageCollect() {
        Iterator<Circle> i = circles.iterator();
        while (i.hasNext()) {
            Circle c = i.next();
            if (c.alpha <= 0) {
                i.remove();
            }
        }
    }

    void attemptAdd() {
        /* Make a random location and diameter */
        PVector newLoc = new PVector(random(width), random(height));
        int newD = (int) random(min, max);

        int attempts = 0;
        /* Detect whether if we use these these values if it will intersect the other objects. */
        while (detectAnyCollision(circles, newLoc, newD)) {
            attempts += 1;
            if (attempts >= 10) {
                return;
            }

            /* If the values do interect make new values. */
            newLoc = new PVector(random(width), random(height));
            newD = (int) random(min, max);
        }

        /* Once we have our values that do not intersect, add a circle. */
        c = new Circle(newLoc, newD, randomColor());
        circles.add(c);
    }

    void evictRandom() {
        Random rnd = new Random();
        int i = rnd.nextInt(circles.size());
        circles.remove(i);
    }

    boolean detectAnyCollision(Collection<Circle> circles, PVector newLoc, int newR) {
        for (Circle c : circles) {
            if (c.detectCollision(newLoc, newR)) {
            return true;
            }
        }
        return false;
    }


    class Circle {
        final PVector loc;
        final int d;
        int c;
        int alpha;

        int alphaRate = 4;

        Circle(PVector loc, int d, int c) {
            this.loc = loc;
            this.d = d;
            this.c = c;

            this.alpha = 1;
        } 

        void draw() {
            /* Random color to add some spice */
            fill(this.c, alpha);
            ellipse(loc.x, loc.y, d, d);

            if (alpha > 255) {
                alphaRate *= -1;
            }

            alpha += alphaRate;
        }

        boolean detectCollision(PVector newLoc, int newD) {
            /* 
            We must divide d + newD because they are both diameters. We want to find what both radius's values are added on. 
            However without it gives the balls a cool forcefeild type gap.
            */
            return dist(loc.x, loc.y, newLoc.x, newLoc.y) < ((d + newD)/2);
        }
    }
}