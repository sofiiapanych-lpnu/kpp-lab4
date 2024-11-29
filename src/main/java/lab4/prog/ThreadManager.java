package lab4.prog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lab4.prog.Areas.*;

import java.util.*;
import java.util.concurrent.*;

public class ThreadManager {
    private List<IArea> availableAreas;
    private ObservableList<ThreadInfoModel> threadInfoList;
    ScheduledThreadPoolExecutor executor;
    ConcurrentHashMap<Integer, double[][]> results;
    private ConcurrentHashMap<Integer, ScheduledFuture<?>> scheduledFutures;
    ExecutorService fixedThreadPool;
    ScheduledExecutorService scheduledExecutor;
    private int taskId;

    public ThreadManager(Runnable uiUpdateCallback) {
        this.availableAreas = Arrays.asList(
                new EastArea(),
                new WestArea(),
                new NorthArea(),
                new SouthArea()
        );
        this.results = new ConcurrentHashMap<>();
        this.threadInfoList = FXCollections.observableArrayList();
        this.executor = new ScheduledThreadPoolExecutor(6);
        this.scheduledFutures = new ConcurrentHashMap<>();
        this.fixedThreadPool = Executors.newFixedThreadPool(4);
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.taskId = 1;
        scheduledExecutor.scheduleAtFixedRate(uiUpdateCallback, 0, 5, TimeUnit.SECONDS);
    }

    public List<IArea> getAvailableAreas() {
        return availableAreas;
    }

    public ObservableList<ThreadInfoModel> getThreadInfoList() {
        return threadInfoList;
    }

    public void addStation(IArea selectedArea, int interval) {
        int stationId = taskId++;
        ThreadInfoModel threadInfo = new ThreadInfoModel("Thread " + stationId, "Waiting queue", "Waiting for result...");
        threadInfoList.add(threadInfo);

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {
            threadInfo.setThreadStatus("Working");
            try {
                Thread.sleep(500);
                double[][] result = selectedArea.generateData();
                synchronized (results) {
                    results.put(stationId, result);
                }
                String resultText = Arrays.deepToString(result);
                threadInfo.setThreadStatus("Sleeping");
                threadInfo.setThreadResult(resultText);
                //uiUpdateCallback.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, interval, TimeUnit.SECONDS);

        scheduledFutures.put(stationId, future);
    }

    public void removeStation(int stationId) {
        ScheduledFuture<?> future = scheduledFutures.remove(stationId);
        if (future != null) {
            future.cancel(true);
            results.remove(stationId);
        }

        ThreadInfoModel threadToRemove = null;
        for (ThreadInfoModel thread : threadInfoList) {
            if (thread.getThreadName().get().equals("Thread " + stationId)) {
                threadToRemove = thread;
                break;
            }
        }

        if (threadToRemove != null) {
            threadInfoList.remove(threadToRemove);
        }
    }


    public double[][] calculateAverageWeather() {
        if (results.isEmpty()) return null;

        List<Future<double[][]>> futures = new ArrayList<>();
        synchronized (results) {
            for (double[][] data : results.values()) {
                futures.add(fixedThreadPool.submit(() -> {
                    int intervalLength = data.length / 24;
                    double[][] result = new double[24][2];
                    for (int i = 0; i < 24; i++) {
                        double tempSum = 0, humiditySum = 0;
                        for (int j = i * intervalLength; j < (i + 1) * intervalLength; j++) {
                            tempSum += data[j][0];
                            humiditySum += data[j][1];
                        }
                        result[i][0] = tempSum / intervalLength;
                        result[i][1] = humiditySum / intervalLength;
                    }
                    return result;
                }));
            }
        }

        double[][] sum = new double[24][2];
        try {
            for (Future<double[][]> future : futures) {
                double[][] arr = future.get();
                for (int i = 0; i < 24; i++) {
                    sum[i][0] += arr[i][0];
                    sum[i][1] += arr[i][1];
                }
            }
            for (int i = 0; i < 24; i++) {
                sum[i][0] /= futures.size();
                sum[i][1] /= futures.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }

    public void shutdown() {
        scheduledFutures.values().forEach(future -> {
            future.cancel(true);
        });
        fixedThreadPool.shutdownNow();
        scheduledExecutor.shutdownNow();
        executor.shutdownNow();
    }
}
