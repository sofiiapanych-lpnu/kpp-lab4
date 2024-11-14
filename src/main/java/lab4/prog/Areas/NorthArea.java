package lab4.prog.Areas;

public class NorthArea implements IArea {
    public NorthArea() {}

    @Override
    public String getName() {
        return "North Area";
    }

    @Override
    public double[][] generateData() {
        int count = (int) (Math.random() * (200 - 24 + 1)) + 24;
        double[][] data = new double[count][2];

        for (int i = 0; i < count; i++) {
            double temperature = Math.round((Math.random() * 10 + 5) * 10.0) / 10.0;; // від 5 до 15 градусів
            double humidity = Math.round((Math.random() * 10 + 70) * 10.0) / 10.0; // від 70 до 80%
            data[i] = new double[]{temperature, humidity};
        }

        return data;
    }

}

