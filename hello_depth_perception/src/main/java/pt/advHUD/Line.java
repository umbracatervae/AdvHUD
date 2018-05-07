package pt.advHUD;

// 2D Line, based on the z and x axes

public class Line {
    private double slope;
    private double intercept;

    public Line(Point p1, Point p2) {
        slope = (p2.z - p1.z) / (p2.x - p1.x);
        intercept = p2.z - (slope * p2.x);
    }

    public Line(double s, double i) {
        slope = s;
        intercept = i;
    }

    public Line() {
        slope = 0;
        intercept = 0;
    }

    public Line(Line rhs) {
        slope = rhs.slope;
        intercept = rhs.intercept;
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    public void setSlope(double s) {
        slope = s;
    }

    public void setIntercept(double i) {
        intercept = i;
    }

    public double getAngle(Line rhs) {
        return Math.atan(Math.abs((rhs.slope - slope) / (1 + rhs.slope * slope)));
    }

    public double getDistance(Point p) {
        double a = getSlope();
        double b = -1;
        double c = getIntercept();

        double bot = Math.sqrt(a * a + b * b);
        double top = Math.abs(a * p.x + b * p.z + c);
        return top / bot;
    }
}
