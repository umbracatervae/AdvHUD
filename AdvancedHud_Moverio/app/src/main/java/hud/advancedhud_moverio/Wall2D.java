package hud.advancedhud_moverio;

public class Wall2D {
    private Point edge1;
    private Point edge2;
    private Line line;
    private boolean isValid;
    private double pointCount;
    
    public Wall2D(Point e) {
        edge1 = e;
        isValid = false;
        pointCount = 1;
    }

    public Wall2D(Point e, Line l) {
        edge1 = e;
        isValid = false;
        pointCount = 1;
        line = l;
    }

    public double getLength() {
        return edge1.dist2D(edge2);
    }

    private void adjustLine() {
        Line tempLine = new Line(edge1, edge2);

        line.setIntercept(line.getIntercept()*((pointCount - 1.0)/pointCount) + (1.0/pointCount)*tempLine.getIntercept());
        line.setSlope(line.getSlope()*((pointCount - 1.0)/pointCount) + (1.0/pointCount)*tempLine.getSlope());
    }

    public void addPoint(Point p) {
        pointCount += 1.0;

        if (isValid == false) {
            edge2 = p;
            line = new Line(edge1, edge2);
        } else {
            double d1 = edge1.dist2D(p);
            double d2 = edge2.dist2D(p);

            isValid = true;

            if (d1 > getLength() || d2 > getLength()) {
                if (d1 > d2)
                    edge2 = p;
                else
                    edge1 = p;

                adjustLine();
            }


        }
    }

    public Point getEdge2() {
        return edge2;
    }

    public Point getEdge1() {
        return edge1;
    }

    public double getPointCount() {
        return pointCount;
    }

    public double getDistance(Point p) {
        if (isValid) {
            double a = line.getSlope();
            double b = -1;
            double c = line.getIntercept();

            double bot = Math.sqrt(a * a + b * b);
            double top = Math.abs(a * p.x + b * p.z + c);

            return top / bot;
        } else
            return edge1.dist2D(p);
    }

    public Line getLine() {
        return line;
    }

    public double getAngle(Wall2D rhs) {
        return line.getAngle(rhs.line);
    }

    public String to_string() {
        return "edge1 = " + edge1 + ", edge2 = " + edge2;
    }

    public double[] sendData() {
        double[] out = {edge1.x, edge1.z, edge2.x, edge2.z};

        return out;
    }
}
