package sum.proj;

public class Point implements Comparable<Point>{
    public int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Point point) {
        int res = Integer.compare(x, point.x);
        if(res != 0)return res;
        return Integer.compare(y, point.y);
    }
}