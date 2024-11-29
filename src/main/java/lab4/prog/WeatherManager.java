package lab4.prog;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lab4.prog.Areas.*;

import java.util.concurrent.ScheduledFuture;

public class WeatherManager {
    @FXML
    private ComboBox<IArea> areaChoiceBox;
    @FXML
    private TextField interval;
    @FXML
    private TableView<ThreadInfoModel> threadTable;
    @FXML
    private TableColumn<ThreadInfoModel, String> threadIdColumn;
    @FXML
    private TableColumn<ThreadInfoModel, String> statusColumn;
    @FXML
    private TableColumn<ThreadInfoModel, String> resultColumn;
    @FXML
    private TableView<WeatherData> currentWeatherTable;
    @FXML
    private TableColumn<WeatherData, String> timeColumn;
    @FXML
    private TableColumn<WeatherData, Double> temperatureColumn;
    @FXML
    private TableColumn<WeatherData, Double> humidityColumn;

    private ThreadManager threadManager;

    @FXML
    public void initialize() {
        threadManager = new ThreadManager(this::updateCurrentWeather);
        areaChoiceBox.getItems().addAll(threadManager.getAvailableAreas());
        threadTable.setItems(threadManager.getThreadInfoList());

        threadIdColumn.setCellValueFactory(cellData -> cellData.getValue().getThreadName());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().getThreadStatus());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().getThreadResult());

        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove Station");
        removeItem.setOnAction(event -> onRemoveStationButtonClick());
        contextMenu.getItems().add(removeItem);

        threadTable.setContextMenu(contextMenu);

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        temperatureColumn.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        humidityColumn.setCellValueFactory(new PropertyValueFactory<>("humidity"));
    }

    @FXML
    protected void onAddStationButtonClick() {
        IArea selectedArea = areaChoiceBox.getValue();
        String intervalText = interval.getText();
        if (intervalText == null || intervalText.trim().isEmpty()) {
            showAlert("Error", "Interval value is required and cannot be empty.");
            return;
        }
        int intervalValue = Integer.parseInt(intervalText.trim());
        if (intervalValue <= 0 || selectedArea == null) {
            showAlert("Error", "Please select an area and enter a positive interval value.");
        } else {
            threadManager.addStation(selectedArea, intervalValue);
        }
    }

    private void updateCurrentWeather() {
        Platform.runLater(() -> {
            double[][] averageWeather = threadManager.calculateAverageWeather();
            if (averageWeather != null) {
                currentWeatherTable.getItems().clear();
                for (int hour = 0; hour < 24; hour++) {
                    currentWeatherTable.getItems().add(new WeatherData(
                            String.format("%02d:00", hour),
                            Math.round(averageWeather[hour][0] * 10) / 10.0,
                            Math.round(averageWeather[hour][1] * 10) / 10.0
                    ));
                }
            }
        });
    }

    @FXML
    protected void onRemoveStationButtonClick() {
        ThreadInfoModel selected = threadTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String threadName = selected.getThreadName().get();
            int stationId = Integer.parseInt(threadName.split(" ")[1]);
            threadManager.removeStation(stationId);
        }
    }





//    private void onRemoveStationFromTable() {
//        ThreadInfoModel selectedThreadInfo = threadTable.getSelectionModel().getSelectedItem();
//
//        if (selectedThreadInfo != null) {
//            String threadName = selectedThreadInfo.getThreadName().get();
//
//            if (threadName.contains(" ")) {
//                String[] parts = threadName.split(" ");
//
//                if (parts.length > 1) {
//                    try {
//                        int stationId = Integer.parseInt(parts[1]);
//
//                        ScheduledFuture<?> futureToCancel = scheduledFutures.get(stationId);
//
//                        if (futureToCancel != null) {
//                            futureToCancel.cancel(true);
//                            scheduledFutures.remove(stationId);
//                            System.out.println("Thread " + stationId + " has been cancelled.");
//                        }
//
//                        threadInfoList.remove(selectedThreadInfo);
//                    } catch (NumberFormatException e) {
//                        System.err.println("Invalid station ID in thread name: " + threadName);
//                    }
//                } else {
//                    System.err.println("Thread name format is incorrect: " + threadName);
//                }
//            } else {
//                System.err.println("No station ID found in thread name: " + threadName);
//            }
//        } else {
//            System.err.println("No thread selected");
//        }
//    }
    public void shutdownExecutors() {
        threadManager.shutdown();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
