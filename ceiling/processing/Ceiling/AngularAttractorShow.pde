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
        color c;
        int radius = 500;

        Dots(PVector _PV)
        {
            location = _PV;
            int j = (int)random(0, 5);
            if (j==0) c = color(#05CDE5);
            if (j==1) c = color(#FFB803);
            if (j==2) c = color(#FF035B);
            if (j==3) c = color(#3D3E3E);
            if (j==4) c = color(#D60FFF);
            float xt = random(-0.002, 0.002);
            float yt = random(-0.002, 0.002);
            velocity = new PVector(xt, yt );
        }

        void display()
        {
            fill(c);
            noStroke();
            ellipse(location.x, location.y, 2, 2);
        }

        void update()
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