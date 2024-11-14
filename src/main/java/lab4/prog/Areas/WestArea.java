package lab4.prog.Areas;

public class WestArea implements IArea {
    public WestArea() {}

    @Override
    public String getName() {
        return "West Area";
    }

    @Override
    public double[][] generateData() {
        int count = (int) (Math.random() * (200 - 24 + 1)) + 24;
        double[][] data = new double[count][2];

        for (int i = 0; i < count; i++) {
            double temperature = Math.round((Math.random() * 15 + 10) * 10.0) / 10.0; // Від 10 до 25 градусів
            double humidity = Math.round((Math.random() * 15 + 40) * 10.0) / 10.0; // Від 40 до 55%
            data[i] = new double[]{temperature, humidity};
        }

        return data;
    }
}

