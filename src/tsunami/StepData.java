package tsunami;

public class StepData {

    private static final int H = 60*60;
    private static final int M = 60;
    private static final int S = 1;

    public int clock, step;
    public double x[], z[], depth[];

    public StepData(int clock, int step, double x[], double z[], double depth[]) {
        this.clock = clock;
        this.step = step;
        this.x = x;
        this.z = z;
        this.depth = depth;
    }

    public void print() {
        System.out.println("StepData("+step+")");
        System.out.println("- Clock: "+String.format("%02d:%02d:%02d", clock/H, clock/M%M, clock%M));;
    }

}
