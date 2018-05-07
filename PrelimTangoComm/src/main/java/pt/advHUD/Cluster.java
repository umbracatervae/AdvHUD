package pt.advHUD;

import pt.advHUD.Plane;
import pt.advHUD.Point;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
 
 private final List<Point> points;
 private Plane plane;
 private Point centroid;
  
 public Cluster(Point firstPoint) {
  points = new ArrayList<Point>();
  centroid = firstPoint;
  calcPlane();
 }

 public void calcPlane() {

  if (points.size() > 2) {
   Point p1 = new Point(0, 0, 0);
   Point p2 = new Point(0, 0, 0);
   Point p3 = new Point(0, 0, 0);
   double c1 = 0, c2 = 0, c3 = 0;

   for (int i = 0; i < points.size(); i += 3) {
    p1.add(points.get(i));
    c1++;

    if (i+1 < points.size()) {
     p2.add(points.get(i+1));
     c2++;
    }

    if (i+2 < points.size()) {
     p3.add(points.get(i+2));
     c3++;
    }
   }

   if (c1 != 0) p1 = new Point(p1.x/c1, p1.y/c1, p1.z/c1);
   if (c2 != 0) p2 = new Point(p2.x/c2, p2.y/c2, p2.z/c2);
   if (c3 != 0) p3 = new Point(p3.x/c3, p3.y/c3, p3.z/c3);

   plane = new Plane(p1, p2, p3);
  }
 }

 public Plane getPlane() {
  return plane;
 }

 public Point getCentroid(){
  return centroid;
 }
  
 public void updateCentroid(){
  double newx = 0d, newy = 0d, newz = 0d;
  for (Point point : points){
   newx += point.x; newy += point.y; newz += point.z;
  }
  centroid = new Point(newx / points.size(), newy / points.size(), newz / points.size());
 }
  
 public List<Point> getPoints() {
  return points;
 }
  
 public String toString(){
  StringBuilder builder = new StringBuilder("This cluster contains the following points:\n");
  for (Point point : points)
   builder.append(point.toString() + ",\n");
  return builder.deleteCharAt(builder.length() - 2).toString(); 
 }
}