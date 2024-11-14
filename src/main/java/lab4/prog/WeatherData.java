package lab4.prog;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class WeatherData {
    private final SimpleStringProperty time;
    private final SimpleDoubleProperty temperature;
    private final SimpleDoubleProperty humidity;

    public WeatherData(String time, double temperature, double humidity) {
        this.time = new SimpleStringProperty(time);
        this.temperature = new SimpleDoubleProperty(temperature);
        this.humidity = new SimpleDoubleProperty(humidity);
    }

    public String getTime() {
        return time.get();
    }

    public double getTemperature() {
        return temperature.get();
    }

    public double getHumidity() {
        return humidity.get();
    }
}

