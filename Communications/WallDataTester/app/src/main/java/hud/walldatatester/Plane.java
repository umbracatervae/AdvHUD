package hud.walldatatester;

public class Plane {
  private Point direction;
  private double shift;

  public Plane() {
    direction = new Point(0, 0, 0);
    shift = 0;
  }

  public Plane(Point a, Point b, Point c) {
    Point ab = new Point(a.x - b.x, a.y - b.y, a.z - b.z);
    Point ac = new Point(a.x - c.x, a.y - c.y, a.z - c.z);
    direction = new Point(ab.y*ac.z - ab.z*ac.y, ab.z*ac.x - ab.x*ac.z, ab.x*ac.y - ab.y*ac.x);
    shift = a.x*direction.x + a.y*direction.y + a.z*direction.z;
//    normalize();
  }

  public void normalize() {
    if (shift != 0) {
      direction.x = direction.x / shift;
      direction.y = direction.y / shift;
      direction.z = direction.z / shift;
      shift = 1;
    }
  }

  double calcPlaneToVectorAngle(Point vector) {
    double topVal = Math.abs(direction.x*vector.x + direction.y*vector.y + direction.z*vector.z);
    double botVal1 = Math.sqrt(direction.x*direction.x + direction.y*direction.y + direction.z*direction.z);
    double botVal2 = Math.sqrt(vector.x*vector.x + vector.y*vector.y + vector.z*vector.z);

    return Math.asin((topVal)/(botVal1 * botVal2));
  }

  public void setDirection(Point p) {
    direction.x = p.x;
    direction.y = p.y;
    direction.z = p.z;
  }

  Point getDirection() {
    return direction;
  }

  public void setShift(double s) {
    shift = s;
  }
  
  public double getShift() {
    return shift;
  }

  // Author: Ryan Stevens
  // returns the acute angle between the planes in radians
  public double calcInterPlaneAngle(Plane inputPlane) throws NullPointerException {
    if (inputPlane == null)
      throw new NullPointerException("Null pointer exception in calcInterPlaneAngle()");

    return Math.acos(Math.abs((this.direction.x * inputPlane.direction.x) + (this.direction.y * inputPlane.direction.y) + (this.direction.z * inputPlane.direction.z) )
            / ( Math.sqrt((this.direction.x * this.direction.x) + (this.direction.y * this.direction.y) + (this.direction.z * this.direction.z))
                * Math.sqrt((inputPlane.direction.x * inputPlane.direction.x) + (inputPlane.direction.y * inputPlane.direction.y) + (inputPlane.direction.z * inputPlane.direction.z))
              ));
  }


  @Override
  public boolean equals(Object other) {
    boolean result = false;
    if (other instanceof Plane) {
      Plane that = (Plane) other;
      result = direction.equals(that.direction) && shift == that.shift;
    }
    return result;
  }

  @Override
  public String toString() {
    return direction.x + "*x + " + direction.y + "*y + " + direction.z + "*z = " + shift;
  }
}
