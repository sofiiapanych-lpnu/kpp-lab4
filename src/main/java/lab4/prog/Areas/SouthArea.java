package lab4.prog.Areas;

public class SouthArea implements IArea{
    public SouthArea() {}

    @Override
    public String getName() {
        return "South Area";
    }

    @Override
    public double[][] generateData() {
        int count = (int) (Math.random() * (200 - 24 + 1)) + 24;
        double[][] data = new double[count][2];

        for (int i = 0; i < count; i++) {
            double temperature = Math.round((Math.random() * 10 + 20) * 10.0) / 10.0; // від 20 до 30 градусів
            double humidity = Math.round((Math.random() * 10 + 50) * 10.0) / 10.0; // від 50 до 60%
            data[i] = new double[]{temperature, humidity};
        }

        return data;
    }

    @Override
    public String toString() {
        return getName();
    }
}
