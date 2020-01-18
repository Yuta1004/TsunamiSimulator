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
        System.out.println("- Clock: "+getStrClock());
    }

    public void printDetail() {
        print();
        System.out.print("- x:");
        for(int idx = 0; idx < x.length; ++ idx)
            System.out.print(" "+x[idx]);
        System.out.print("\n- z:");
        for(int idx = 0; idx < z.length; ++ idx)
            System.out.print(" "+z[idx]);
        System.out.print("\n- depth:");
        for(int idx = 0; idx < z.length; ++ idx)
            System.out.print(" "+depth[idx]);
        System.out.println();
    }

    public String getStrClock() {
        return
            String.format("%02d", clock/H)+":"+
            String.format("%02d", clock%H/M)+":"+
            String.format("%02d", clock%M);
    }

}
