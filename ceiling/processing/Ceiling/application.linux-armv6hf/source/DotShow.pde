

/**
 * A Ceiling show that draws a dot following the cursor.
 */
public class DotShow extends Show {
  private final PImage dot;

  public DotShow() {
    this.dot = loadImage("dot.png");
  }

    public void draw() {     
        background(0);

        // Draw the image, centered at the mouse location
        float dotSize = height * 0.4;
        image(dot, mouseX - dotSize/2, mouseY - dotSize/2, dotSize, dotSize);
    }
}