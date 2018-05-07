package pt.advHUD;

import pt.advHUD.Cluster;

public class Wall {
    private Point edge1;
    private Point edge2;
    private Plane plane;
    private boolean isValid;
    
    public Wall(Cluster cluster) {
        isValid = false;
        plane = cluster.getPlane();
        edge1 = cluster.getCentroid();
    }

    public double getLength() {
        return edge1.dist2D(edge2);
    }

    public boolean isValid() {
        return isValid;
    }

    public Plane getPlane() {
        return plane;
    }

    public Point getEdge2() {
        return edge2;
    }

    public Point getEdge1() {
        return edge1;
    }

    public void update(Cluster cluster) {
        if (cluster == null)
            return;

        Point centroid = cluster.getCentroid();

        if (!isValid) {
            edge2 = centroid;
            isValid = true;
        } else {
            double d1 = edge1.dist2D(centroid);
            double d2 = edge2.dist2D(centroid);

            if (d1 > getLength() || d2 > getLength()) {
                if (d1 > d2)
                    edge2 = centroid;
                else
                    edge1 = centroid;
            }

//            Point mid = new Point(0.5 * edge1.x + 0.5 * edge2.x, 0.5 * edge1.y + 0.5 * edge2.y, 0);
//            if (mid.dist2D(centroid) > mid.dist2D(edge1)) {
//                if (edge1.dist2D(centroid) > edge2.dist2D(centroid))
//                    edge2 = centroid;
//                else
//                    edge1 = centroid;
//            }
        }
//        plane.setDirection(new Point(plane.getDirection().x*0.5 + cluster.getPlane().getDirection().x*0.5, plane.getDirection().x*0.5
//                                     + cluster.getPlane().getDirection().y*0.5, plane.getDirection().z*0.5
//                                     + cluster.getPlane().getDirection().z*0.5)); // averaging the plane equation
////        plane.setShift(cluster.getPlane().getShift()*0.5 + plane.getShift()*0.5);
////        plane.normalize();
    }
    
    public double[] sendData() {
        double[] out = {edge1.x, edge1.y, edge2.x, edge2.y};
        
        return out;
    }
}
