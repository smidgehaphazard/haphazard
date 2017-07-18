OPC opc;
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
 
void setup()
{
  
  // This will all be dithered and interpolated anyway.
  // Pick a convenient size to display on a screen with some margin
  // 
  size(768, 1024);

  dot = loadImage("dot.png");
  opc = new OPC(this, "127.0.0.1", 7890);

  
  //opc.ledGrid(0, 50, 10, x, y, ledSpacing, stripSpacing, 0, false);
}

void draw()
{
  background(0);

  // Draw the image, centered at the mouse location
  //float dotSize = height * 0.7;
  //image(dot, mouseX - dotSize/2, mouseY - dotSize/2, dotSize, dotSize);
 
 
  // XXX registerDraw is borked  
}

void registerDraw() {
  // XXX borked
}