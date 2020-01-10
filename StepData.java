public class StepData {

    private static final int H = 60*60;
    private static final int M = 60;
    private static final int S = 1;

    public int clock, step;
    public double x[], z[];

    public StepData(int clock, int step, double x[], double z[]) {
        this.clock = clock;
        this.step = step;
        this.x = x;
        this.z = z;
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
        System.out.println();
    }

    private String getStrClock() {
        return
            String.format("%02d", clock/H)+":"+
            String.format("%02d", clock%H/M)+":"+
            String.format("%02d", clock%M);
    }

}
