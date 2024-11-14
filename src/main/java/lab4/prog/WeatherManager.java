package lab4.prog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lab4.prog.Areas.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WeatherManager {
    @FXML
    private Label currentWeather;
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
    @FXML
    private ComboBox<IArea> areaChoiceBox;

    private List<IArea> availableAreas;
    private ObservableList<ThreadInfoModel> threadInfoList;
    private ScheduledThreadPoolExecutor executor;

    private int taskId = 1;
    private ConcurrentHashMap<Integer, double[][]> results;
    private ConcurrentHashMap<Integer, ScheduledFuture<?>> scheduledFutures;

    @FXML
    public void initialize() {
        availableAreas = Arrays.asList(
                new EastArea(),
                new WestArea(),
                new NorthArea(),
                new SouthArea()
        );
        areaChoiceBox.getItems().addAll(availableAreas);
        areaChoiceBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(IArea item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        areaChoiceBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(IArea item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove Station");
        removeItem.setOnAction(event -> onRemoveStationFromTable());
        contextMenu.getItems().add(removeItem);

        threadTable.setContextMenu(contextMenu);
        interval.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                interval.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        results = new ConcurrentHashMap<>();
        threadInfoList = FXCollections.observableArrayList();
        threadTable.setItems(threadInfoList);

        threadIdColumn.setCellValueFactory(cellData -> cellData.getValue().getThreadName());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().getThreadStatus());
        resultColumn.setCellValueFactory(cellData -> cellData.getValue().getThreadResult());
        executor = new ScheduledThreadPoolExecutor(6);

        scheduledFutures = new ConcurrentHashMap<>();
        scheduledExecutor.scheduleAtFixedRate(this::countAverageWeather, 0, 5, TimeUnit.SECONDS);

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        temperatureColumn.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        humidityColumn.setCellValueFactory(new PropertyValueFactory<>("humidity"));
    }

    @FXML
    protected void onAddStationButtonClick() {
        IArea selectedArea = areaChoiceBox.getValue();
        int f_i = taskId;
        ThreadInfoModel threadInfo = new ThreadInfoModel("Thread " + f_i, "Waiting queue", "Waiting for result...", "None");
        Platform.runLater(() -> threadInfoList.add(threadInfo));
        //startThreadInfoUpdate();
        int interv = Integer.parseInt(interval.getText());


        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {

            Platform.runLater(() -> {
                threadInfo.setThreadStatus("Working");
            });
            try {
                Thread.sleep(500);
                double[][] result = selectedArea.generateData();
                synchronized (results) {
                    results.put(f_i, result);
                }


                String resultText = Arrays.deepToString(result);

                Platform.runLater(() -> {
                    threadInfo.setThreadStatus("Sleeping");
                    threadInfo.setThreadResult(resultText);
                });

                System.out.println("Station " + f_i + " data: " + resultText);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, interv, TimeUnit.SECONDS);

        taskId++;
        scheduledFutures.put(taskId-1, future);
    }

    private synchronized void updateUIWithResult(int stationId, double[] result) {

        String resultText = "Station " + stationId + " Current Weather: " + Arrays.toString(result);
        currentWeather.setText(resultText);
    }


    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
    private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    private void countAverageWeather() {
        if(results.size()==0){
            return;
        }
        List<Future<double[][]>> futures = new ArrayList<>();
        synchronized (results) {
            for (Map.Entry<Integer, double[][]> entry : results.entrySet()) {
                double[][] data = entry.getValue();

                Callable<double[][]> task = () -> {
                    int intervalLength = data.length / 24;
                    double[][] result = new double[24][2];

                    for (int i = 0; i < 24; i++) {
                        double tempSum = 0;
                        double humiditySum = 0;

                        for (int j = i * intervalLength; j < (i + 1) * intervalLength; j++) {
                            tempSum += data[j][0];
                            humiditySum += data[j][1];
                        }

                        result[i][0] = tempSum / intervalLength;
                        result[i][1] = humiditySum / intervalLength;
                    }

                    return result;
                };

                futures.add(fixedThreadPool.submit(task));
            }
        }
        double[][] sum = new double[24][2];

        for (Future<double[][]> future : futures) {
            try {
                double[][] arr = future.get();
                for (int i = 0; i < 24; i++) {
                    sum[i][0] += arr[i][0];
                    sum[i][1] += arr[i][1];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 24; i++) {
            sum[i][0] /= futures.size();
            sum[i][1] /= futures.size();
        }

        Platform.runLater(() -> {
            currentWeatherTable.getItems().clear();
            for (int hour = 0; hour < 24; hour++) {
                double temperature = Math.round(sum[hour][0] * 10.0) / 10.0;
                double humidity = Math.round(sum[hour][1] * 10.0) / 10.0;
                String time = String.format("%02d:00", hour);
                currentWeatherTable.getItems().add(new WeatherData(time, temperature, humidity));
            }
        });

    }

    public void shutdownExecutors() {
        scheduledFutures.forEach((k,v)->{  v.cancel(true);});
        fixedThreadPool.shutdownNow();
        scheduledExecutor.shutdownNow();
        executor.shutdownNow();


    }

    private void onRemoveStationFromTable() {
        ThreadInfoModel selectedThreadInfo = threadTable.getSelectionModel().getSelectedItem();

        if (selectedThreadInfo != null) {
            String threadName = selectedThreadInfo.getThreadName().get();

            if (threadName.contains(" ")) {
                String[] parts = threadName.split(" ");

                if (parts.length > 1) {
                    try {
                        int stationId = Integer.parseInt(parts[1]);

                        ScheduledFuture<?> futureToCancel = scheduledFutures.get(stationId);

                        if (futureToCancel != null) {
                            futureToCancel.cancel(true);
                            scheduledFutures.remove(stationId);
                            System.out.println("Thread " + stationId + " has been cancelled.");
                        }

                        threadInfoList.remove(selectedThreadInfo);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid station ID in thread name: " + threadName);
                    }
                } else {
                    System.err.println("Thread name format is incorrect: " + threadName);
                }
            } else {
                System.err.println("No station ID found in thread name: " + threadName);
            }
        } else {
            System.err.println("No thread selected");
        }
    }


}
