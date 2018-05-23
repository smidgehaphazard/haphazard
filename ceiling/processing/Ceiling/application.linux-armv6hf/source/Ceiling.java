import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import oscP5.*; 
import netP5.*; 
import java.net.*; 
import java.util.Arrays; 
import java.util.*; 
import java.time.Duration; 
import java.time.Instant; 

import netP5.*; 
import oscP5.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Ceiling extends PApplet {





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

public void setup()
{
  // Keep in sync with constants above (this bit of processing kind of sucks)
  // size(contentWidth + (2 * contentMargin), contentHeight + (2 * contentMargin));  
  

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

public void draw()
{
  // TODO(shawn) smooth scene changes between shows.

  if (looping) {
    this.showLoopController.currentShow.draw();
  } else {
    currentShow.draw();
  }
}

public void keyPressed() {
  if (looping && key == this.nextShowKey) {
    this.showLoopController.nextShow();
  }
}

public void oscEvent(OscMessage msg) {
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
/**
 * Adapted from https://www.openprocessing.org/sketch/444574
 */
public class AngularAttractorShow extends Show {
    private final List poop;
    private final float distance = 150;
    private final int maxInteractions = 2;
    private final boolean flag = true;

    public AngularAttractorShow() {
        this.poop = new ArrayList();
        for (int i=0; i < 150; i++) {
            PVector pd = new PVector(random(-400, 400), random(-400, 400));
            Dots dots = new Dots(pd);
            poop.add(dots);
        }
    }

    public void draw() {     
        background(0);
        translate(width/2, height/2);

        for (int i=0; i<poop.size(); i++) {
            Dots dots1 = (Dots) poop.get(i);
            dots1.display();
            dots1.update();

            int interactions = 0;
            for (int j=i+1; j < poop.size(); j++) {
              Dots dots2 = (Dots) poop.get(j);
              dots2.update();
  
              float dist_1_2 = dist(dots1.location.x, dots1.location.y, dots2.location.x, dots2.location.y);
              if (dist_1_2 < distance) {
                interactions += 1;
  
                for (int k=j+1; k < poop.size(); k++) {
                  Dots dots3 = (Dots) poop.get(k);
                  dots3.update();
  
                  float dist_2_3 = dist(dots3.location.x, dots3.location.y, dots2.location.x, dots2.location.y);
                  if (dist_2_3 < distance) {
                    if (interactions > maxInteractions) {
                      // Let all the poop interact with each other for velocity purposes but cap
                      // the amout of visual interactions.
                      continue;
                    }

                    if (flag) {
                      // Gradually fade in the color based on distance
                      // TODO(shawn) not perfect, consider triangular interpolation rather than linear interpolation
                      // so that 
                      float dist_1_2_norm = (distance - dist_1_2) / distance;
                      float dist_2_3_norm = (distance - dist_2_3) / distance;
                      
                      // Triangle interpolation lets triangles flicker in and out of existance.
                      // float weighted = (dist_1_2_norm + dist_2_3_norm) / 2;
                      
                      float min = min(dist_1_2_norm, dist_2_3_norm);
                      
                      int alpha = (int)(min * 50); 
                      fill(dots3.c, alpha);
                      noStroke();
                    } else {
                      noFill();
                      stroke(255,50);
                    }
                    
                    beginShape();
                    vertex(dots3.location.x, dots3.location.y);
                    vertex(dots2.location.x, dots2.location.y);
                    vertex(dots1.location.x, dots1.location.y);
                    endShape();
                  }
              }
          }
        }
      }
    }

    class Dots {
        PVector location;
        PVector velocity;
        int c;
        int radius = 500;

        Dots(PVector _PV)
        {
            location = _PV;
            int j = (int)random(0, 5);
            if (j==0) c = color(0xff05CDE5);
            if (j==1) c = color(0xffFFB803);
            if (j==2) c = color(0xffFF035B);
            if (j==3) c = color(0xff3D3E3E);
            if (j==4) c = color(0xffD60FFF);
            float xt = random(-0.002f, 0.002f);
            float yt = random(-0.002f, 0.002f);
            velocity = new PVector(xt, yt );
        }

        public void display()
        {
            fill(c);
            noStroke();
            ellipse(location.x, location.y, 2, 2);
        }

        public void update()
        {
            if (dist(location.x, location.y, 0, 0) > radius) {
                velocity.mult(-1);
                location.add(velocity);
            } else {
                location.add(velocity);
            }
        }
    }
}
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

    public void draw() {        
        garbageCollect();
        attemptAdd();
        
        background(0);
        for (Circle c: circles) {
            c.draw();
        }
    }
    
    public int randomColor() {
        // Purples
        return color(random(255), 0, random(75, 255));

        // Cool blue greens
        //return color(0, random(255), random(75, 255));

        // Muted teals
        //return color(100, 150, random(150,255));
    }

    public void garbageCollect() {
        Iterator<Circle> i = circles.iterator();
        while (i.hasNext()) {
            Circle c = i.next();
            if (c.alpha <= 0) {
                i.remove();
            }
        }
    }

    public void attemptAdd() {
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

    public void evictRandom() {
        Random rnd = new Random();
        int i = rnd.nextInt(circles.size());
        circles.remove(i);
    }

    public boolean detectAnyCollision(Collection<Circle> circles, PVector newLoc, int newR) {
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

        public void draw() {
            /* Random color to add some spice */
            fill(this.c, alpha);
            ellipse(loc.x, loc.y, d, d);

            if (alpha > 255) {
                alphaRate *= -1;
            }

            alpha += alphaRate;
        }

        public boolean detectCollision(PVector newLoc, int newD) {
            /* 
            We must divide d + newD because they are both diameters. We want to find what both radius's values are added on. 
            However without it gives the balls a cool forcefeild type gap.
            */
            return dist(loc.x, loc.y, newLoc.x, newLoc.y) < ((d + newD)/2);
        }
    }
}


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
        float dotSize = height * 0.4f;
        image(dot, mouseX - dotSize/2, mouseY - dotSize/2, dotSize, dotSize);
    }
}
/*
 * Simple Open Pixel Control client for Processing,
 * designed to sample each LED's color from some point on the canvas.
 *
 * Micah Elizabeth Scott, 2013
 * This file is released into the public domain.
 */




public class OPC implements Runnable
{
  Thread thread;
  Socket socket;
  OutputStream output, pending;
  String host;
  int port;

  int[] pixelLocations;
  byte[] packetData;
  byte firmwareConfig;
  String colorCorrection;
  boolean enableShowLocations;

  OPC(PApplet parent, String host, int port)
  {
    this.host = host;
    this.port = port;
    thread = new Thread(this);
    thread.start();
    this.enableShowLocations = true;
    parent.registerMethod("draw", this);
  }

  // Set the location of a single LED
  public void led(int index, int x, int y)  
  {
    // For convenience, automatically grow the pixelLocations array. We do want this to be an array,
    // instead of a HashMap, to keep draw() as fast as it can be.
    if (pixelLocations == null) {
      pixelLocations = new int[index + 1];
    } else if (index >= pixelLocations.length) {
      pixelLocations = Arrays.copyOf(pixelLocations, index + 1);
    }

    pixelLocations[index] = x + width * y;
  }
  
  // Set the location of several LEDs arranged in a strip.
  // Angle is in radians, measured clockwise from +X.
  // (x,y) is the center of the strip.
  public void ledStrip(int index, int count, float x, float y, float spacing, float angle, boolean reversed)
  {
    float s = sin(angle);
    float c = cos(angle);
    for (int i = 0; i < count; i++) {
      led(reversed ? (index + count - 1 - i) : (index + i),
        (int)(x + (i - (count-1)/2.0f) * spacing * c + 0.5f),
        (int)(y + (i - (count-1)/2.0f) * spacing * s + 0.5f));
    }
  }

  // Set the locations of a ring of LEDs. The center of the ring is at (x, y),
  // with "radius" pixels between the center and each LED. The first LED is at
  // the indicated angle, in radians, measured clockwise from +X.
  public void ledRing(int index, int count, float x, float y, float radius, float angle)
  {
    for (int i = 0; i < count; i++) {
      float a = angle + i * 2 * PI / count;
      led(index + i, (int)(x - radius * cos(a) + 0.5f),
        (int)(y - radius * sin(a) + 0.5f));
    }
  }

  // Set the location of several LEDs arranged in a grid. The first strip is
  // at 'angle', measured in radians clockwise from +X.
  // (x,y) is the center of the grid.
  public void ledGrid(int index, int stripLength, int numStrips, float x, float y,
               float ledSpacing, float stripSpacing, float angle, boolean zigzag)
  {
    float s = sin(angle + HALF_PI);
    float c = cos(angle + HALF_PI);
    for (int i = 0; i < numStrips; i++) {
      ledStrip(index + stripLength * i, stripLength,
        x + (i - (numStrips-1)/2.0f) * stripSpacing * c,
        y + (i - (numStrips-1)/2.0f) * stripSpacing * s, ledSpacing,
        angle, zigzag && (i % 2) == 1);
    }
  }

  // Set the location of 64 LEDs arranged in a uniform 8x8 grid.
  // (x,y) is the center of the grid.
  public void ledGrid8x8(int index, float x, float y, float spacing, float angle, boolean zigzag)
  {
    ledGrid(index, 8, 8, x, y, spacing, spacing, angle, zigzag);
  }

  // Should the pixel sampling locations be visible? This helps with debugging.
  // Showing locations is enabled by default. You might need to disable it if our drawing
  // is interfering with your processing sketch, or if you'd simply like the screen to be
  // less cluttered.
  public void showLocations(boolean enabled)
  {
    enableShowLocations = enabled;
  }
  
  // Enable or disable dithering. Dithering avoids the "stair-stepping" artifact and increases color
  // resolution by quickly jittering between adjacent 8-bit brightness levels about 400 times a second.
  // Dithering is on by default.
  public void setDithering(boolean enabled)
  {
    if (enabled)
      firmwareConfig &= ~0x01;
    else
      firmwareConfig |= 0x01;
    sendFirmwareConfigPacket();
  }

  // Enable or disable frame interpolation. Interpolation automatically blends between consecutive frames
  // in hardware, and it does so with 16-bit per channel resolution. Combined with dithering, this helps make
  // fades very smooth. Interpolation is on by default.
  public void setInterpolation(boolean enabled)
  {
    if (enabled)
      firmwareConfig &= ~0x02;
    else
      firmwareConfig |= 0x02;
    sendFirmwareConfigPacket();
  }

  // Put the Fadecandy onboard LED under automatic control. It blinks any time the firmware processes a packet.
  // This is the default configuration for the LED.
  public void statusLedAuto()
  {
    firmwareConfig &= 0x0C;
    sendFirmwareConfigPacket();
  }    

  // Manually turn the Fadecandy onboard LED on or off. This disables automatic LED control.
  public void setStatusLed(boolean on)
  {
    firmwareConfig |= 0x04;   // Manual LED control
    if (on)
      firmwareConfig |= 0x08;
    else
      firmwareConfig &= ~0x08;
    sendFirmwareConfigPacket();
  } 

  // Set the color correction parameters
  public void setColorCorrection(float gamma, float red, float green, float blue)
  {
    colorCorrection = "{ \"gamma\": " + gamma + ", \"whitepoint\": [" + red + "," + green + "," + blue + "]}";
    sendColorCorrectionPacket();
  }
  
  // Set custom color correction parameters from a string
  public void setColorCorrection(String s)
  {
    colorCorrection = s;
    sendColorCorrectionPacket();
  }

  // Send a packet with the current firmware configuration settings
  public void sendFirmwareConfigPacket()
  {
    if (pending == null) {
      // We'll do this when we reconnect
      return;
    }
 
    byte[] packet = new byte[9];
    packet[0] = (byte)0x00; // Channel (reserved)
    packet[1] = (byte)0xFF; // Command (System Exclusive)
    packet[2] = (byte)0x00; // Length high byte
    packet[3] = (byte)0x05; // Length low byte
    packet[4] = (byte)0x00; // System ID high byte
    packet[5] = (byte)0x01; // System ID low byte
    packet[6] = (byte)0x00; // Command ID high byte
    packet[7] = (byte)0x02; // Command ID low byte
    packet[8] = (byte)firmwareConfig;

    try {
      pending.write(packet);
    } catch (Exception e) {
      dispose();
    }
  }

  // Send a packet with the current color correction settings
  public void sendColorCorrectionPacket()
  {
    if (colorCorrection == null) {
      // No color correction defined
      return;
    }
    if (pending == null) {
      // We'll do this when we reconnect
      return;
    }

    byte[] content = colorCorrection.getBytes();
    int packetLen = content.length + 4;
    byte[] header = new byte[8];
    header[0] = (byte)0x00;               // Channel (reserved)
    header[1] = (byte)0xFF;               // Command (System Exclusive)
    header[2] = (byte)(packetLen >> 8);   // Length high byte
    header[3] = (byte)(packetLen & 0xFF); // Length low byte
    header[4] = (byte)0x00;               // System ID high byte
    header[5] = (byte)0x01;               // System ID low byte
    header[6] = (byte)0x00;               // Command ID high byte
    header[7] = (byte)0x01;               // Command ID low byte

    try {
      pending.write(header);
      pending.write(content);
    } catch (Exception e) {
      dispose();
    }
  }

  // Automatically called at the end of each draw().
  // This handles the automatic Pixel to LED mapping.
  // If you aren't using that mapping, this function has no effect.
  // In that case, you can call setPixelCount(), setPixel(), and writePixels()
  // separately.
  public void draw()
  {
    if (pixelLocations == null) {
      // No pixels defined yet
      return;
    }
    if (output == null) {
      return;
    }

    int numPixels = pixelLocations.length;
    int ledAddress = 4;

    setPixelCount(numPixels);
    loadPixels();

    for (int i = 0; i < numPixels; i++) {
      int pixelLocation = pixelLocations[i];
      int pixel = pixels[pixelLocation];

      packetData[ledAddress] = (byte)(pixel >> 16);
      packetData[ledAddress + 1] = (byte)(pixel >> 8);
      packetData[ledAddress + 2] = (byte)pixel;
      ledAddress += 3;

      if (enableShowLocations) {
        pixels[pixelLocation] = 0xFFFFFF ^ pixel;
      }
    }

    writePixels();

    if (enableShowLocations) {
      updatePixels();
    }
  }
  
  // Change the number of pixels in our output packet.
  // This is normally not needed; the output packet is automatically sized
  // by draw() and by setPixel().
  public void setPixelCount(int numPixels)
  {
    int numBytes = 3 * numPixels;
    int packetLen = 4 + numBytes;
    if (packetData == null || packetData.length != packetLen) {
      // Set up our packet buffer
      packetData = new byte[packetLen];
      packetData[0] = (byte)0x00;              // Channel
      packetData[1] = (byte)0x00;              // Command (Set pixel colors)
      packetData[2] = (byte)(numBytes >> 8);   // Length high byte
      packetData[3] = (byte)(numBytes & 0xFF); // Length low byte
    }
  }
  
  // Directly manipulate a pixel in the output buffer. This isn't needed
  // for pixels that are mapped to the screen.
  public void setPixel(int number, int c)
  {
    int offset = 4 + number * 3;
    if (packetData == null || packetData.length < offset + 3) {
      setPixelCount(number + 1);
    }

    packetData[offset] = (byte) (c >> 16);
    packetData[offset + 1] = (byte) (c >> 8);
    packetData[offset + 2] = (byte) c;
  }
  
  // Read a pixel from the output buffer. If the pixel was mapped to the display,
  // this returns the value we captured on the previous frame.
  public int getPixel(int number)
  {
    int offset = 4 + number * 3;
    if (packetData == null || packetData.length < offset + 3) {
      return 0;
    }
    return (packetData[offset] << 16) | (packetData[offset + 1] << 8) | packetData[offset + 2];
  }

  // Transmit our current buffer of pixel values to the OPC server. This is handled
  // automatically in draw() if any pixels are mapped to the screen, but if you haven't
  // mapped any pixels to the screen you'll want to call this directly.
  public void writePixels()
  {
    if (packetData == null || packetData.length == 0) {
      // No pixel buffer
      return;
    }
    if (output == null) {
      return;
    }

    try {
      output.write(packetData);
    } catch (Exception e) {
      dispose();
    }
  }

  public void dispose()
  {
    // Destroy the socket. Called internally when we've disconnected.
    // (Thread continues to run)
    if (output != null) {
      println("Disconnected from OPC server");
    }
    socket = null;
    output = pending = null;
  }

  public void run()
  {
    // Thread tests server connection periodically, attempts reconnection.
    // Important for OPC arrays; faster startup, client continues
    // to run smoothly when mobile servers go in and out of range.
    for(;;) {

      if(output == null) { // No OPC connection?
        try {              // Make one!
          socket = new Socket(host, port);
          socket.setTcpNoDelay(true);
          pending = socket.getOutputStream(); // Avoid race condition...
          println("Connected to OPC server");
          sendColorCorrectionPacket();        // These write to 'pending'
          sendFirmwareConfigPacket();         // rather than 'output' before
          output = pending;                   // rest of code given access.
          // pending not set null, more config packets are OK!
        } catch (ConnectException e) {
          dispose();
        } catch (IOException e) {
          dispose();
        }
      }

      // Pause thread to avoid massive CPU load
      try {
        Thread.sleep(500);
      }
      catch(InterruptedException e) {
      }
    }
  }
}

/**
 * A degenerate duplication of the sketch API.
 *
 * There's almost certianly a better way to do this.
 */
public abstract class Show {
    public abstract void draw();
}


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

public enum Shows {
    ANGULAR_ATTRACTOR,
    CIRCLE_SPLATTER,
    DOT,
    SOLID,
}
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
  public void settings() {  size(1224, 883); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Ceiling" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
