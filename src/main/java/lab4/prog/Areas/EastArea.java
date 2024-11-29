package lab4.prog.Areas;

public class EastArea implements IArea {
    public EastArea() {}

    @Override
    public String getName() {
        return "East Area";
    }

    @Override
    public double[][] generateData() {
        int count = (int) (Math.random() * (200 - 24 + 1)) + 24;
        double[][] data = new double[count][2];

        for (int i = 0; i < count; i++) {
            double temperature = Math.round((Math.random() * 8 + 15) * 10.0) / 10.0; // від 15 до 23 градусів
            double humidity = Math.round((Math.random() * 20 + 60) * 10.0) / 10.0; // від 60 до 80%
            data[i] = new double[]{temperature, humidity};
        }

        return data;
    }

    @Override
    public String toString() {
        return getName();
    }
}

