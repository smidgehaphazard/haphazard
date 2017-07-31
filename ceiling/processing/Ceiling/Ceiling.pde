// Fadecandy open pixel control client.
OPC opc;

// The current show the ceiling sketch delegates to to render.
Show currentShow;

PImage dot;

/**
 * Layout:
 *
 *  10 strips or 50 pixels
 *  +-----------------------+
 *  1.0 - - - - - - - - - - |
 *  1.1 - - - - - - - - - - |
 *  1.2 - - - - - - - - - - |
 *  ...
 *  2.0 - - - - - - - - - - |
 *  ...
 *  +-----------------------|
 * 
 */ 
 

// ceiling is a 1:0.66 aspect ratio (10' x 15'), keep the content area the same aspect
// Open pixel control will sample the content area
final int contentWidth = 1024;
final int contentHeight = 683;

// inset the content so we can make full-bleed effects.
final int contentMargin = 100;

final int canvasWidth = contentWidth + (contentMargin * 2);
final int canvasHeight = contentHeight + (contentMargin * 2);


// TODO(shawn) show selection and cross fading between shows.

void setup()
{
  // Keep in sync with constants above (this bit of processing kind of sucks)
  // size(contentWidth + (2 * contentMargin), contentHeight + (2 * contentMargin));
  size(1224, 883);

  // Yellow amber
  currentShow = new SolidShow(color(255, 231, 117));

  //dot = loadImage("dot.png");
  opc = new OPC(this, "127.0.0.1", 7890);

  int x = (width / 2);
  int y = (height / 2);
  int ledSpacing = contentWidth / (50 - 1); // 50 pixels, 49 gaps
  int stripSpacing = contentHeight / (10 - 1); // 10 strips, 9 gaps
  opc.ledGrid(0, 50, 10, x, y, ledSpacing, stripSpacing, 0, false);

  
  // Comment out the next line for debugging
  opc.showLocations(false);
}

void draw()
{
  currentShow.draw();
}

void drawDebugGrid() {
  stroke(100);
  fill(0);
  rect(contentMargin, contentMargin, contentWidth, contentHeight);
  
  fill(100);
  
  int endX = width - contentMargin;
  int xStride = contentWidth / (50 - 1); // 50 pixels, 49 gaps
  
  int endY = height - contentMargin;
  int yStride = contentHeight / (10 - 1); // 10 strips, 9 gaps 
  
  for(int row = 0; row < 10; row++) {
    for(int col = 0; col < 50; col++) {
      int x = contentMargin + (col * xStride);
      int y = contentMargin + (row * yStride);
      ellipse(x, y, 10, 10);
    }
  }
}