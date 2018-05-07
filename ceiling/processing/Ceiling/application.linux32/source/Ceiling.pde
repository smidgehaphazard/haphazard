import oscP5.*;
import netP5.*;


// Fadecandy open pixel control client.
OPC opc;

// Touch OSC server;
OscP5 oscP5;

ShowFactory showFactory;

// The current show the ceiling sketch delegates to to render.
Show currentShow;

/**
 * Layout:
 *
 *  10 strips of 50 pixels
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

char nextShowKey = 'n';
boolean loop;
ShowLoopController showLoopController;

void setup()
{
  // Keep in sync with constants above (this bit of processing kind of sucks)
  // size(contentWidth + (2 * contentMargin), contentHeight + (2 * contentMargin));  
  size(1224, 883);

  showFactory = new ShowFactory();
  showLoopController = new ShowLoopController(showFactory, Duration.ofMinutes(5));
  looping = true;

  // The Touch OSC server listening on 8000.
  oscP5 = new OscP5(this,8000);

  // The OPC client connecting to a server running locally.
  opc = new OPC(this, "127.0.0.1", 7890);

  int x = (width / 2);
  int y = (height / 2);
  int ledSpacing = contentWidth / (50 - 1); // 50 pixels, 49 gaps
  int stripSpacing = contentHeight / (10 - 1); // 10 strips, 9 gaps
  opc.ledGrid(0, 50, 10, x, y, ledSpacing, stripSpacing, 0, false);

  
  // Comment out the next line for debugging
  opc.showLocations(true);
  
   background(0);
}

void draw()
{
  // TODO(shawn) smooth scene changes between shows.

  if (looping) {
    this.showLoopController.currentShow.draw();
  } else {
    currentShow.draw();
  }
}

void keyPressed() {
  if (looping && key == this.nextShowKey) {
    this.showLoopController.nextShow();
  }
}

void oscEvent(OscMessage msg) {
    String addr = msg.addrPattern();
    // xxx blah :(
    switch(addr) {
      case "/dot":
      case "/dot/cursor":
      case "/loop":
      case "/solid":
      case "/solid/hsv":
      case "/solid/yellow":
      case "/solid/blue":
      case "/solid/green":
      case "/solid/pink":
      case "/solid/white":
      
      default:
        // Don't throw since clients can send anything
        break;
    }
    //if(addr.equals("/1/fader1"))        { v_fader1 = val; }
    //else if(addr.equals("/1/fader2"))   { v_fader2 = val; }
    //else if(addr.equals("/1/fader3"))   { v_fader3 = val; }
    //else if(addr.equals("/1/fader4"))   { v_fader4 = val; }
    //else if(addr.equals("/1/fader5"))   { v_fader5 = val; }
    //else if(addr.equals("/1/toggle1"))  { v_toggle1 = val; }
}