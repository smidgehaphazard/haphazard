/**
 * A Ceiling show that fills the canvas with a solid color.
 */
public class SolidShow extends Show {
  private final int bgColor;

  /**
   * Created with a color parameter (any value returned by `#color`).
   */
  public SolidShow(int bgColor) {
    this.bgColor = bgColor;
  }

    public void draw() {     
      background(this.bgColor);
    }
}