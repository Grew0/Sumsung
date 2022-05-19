package sum.proj;

public class Round {
    float x, y, r;

    static boolean Round_x_Round(Round a, Round b){
        return sq_of_hypotenuse(a.x-b.x, a.y-b.y) <= (a.r+b.r)*(a.r+b.r);
    }

    static boolean Round_x_Round(float x1, float y1, float r1, float x2, float y2, float r2){
        return sq_of_hypotenuse(x1-x2, y1-y2) <= (r1+r2)*(r1+r2);
    }

    private static float sq_of_hypotenuse(float a, float b){
        return a*a + b*b;
    }
}