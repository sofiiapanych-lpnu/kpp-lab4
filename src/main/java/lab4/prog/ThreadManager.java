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
    Semaphore semaphore;
    Runnable uiUpdateCallback;

    public ThreadManager(Runnable uiUpdateCallback) {
        this.availableAreas = Arrays.asList(
                new EastArea(),
                new WestArea(),
                new NorthArea(),
                new SouthArea()
        );
        this.results = new ConcurrentHashMap<>();
        this.threadInfoList = FXCollections.observableArrayList();
        this.executor = new ScheduledThreadPoolExecutor(2);
        this.scheduledFutures = new ConcurrentHashMap<>();
        this.fixedThreadPool = Executors.newFixedThreadPool(4);
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.taskId = 1;
        scheduledExecutor.scheduleAtFixedRate(uiUpdateCallback, 0, 5, TimeUnit.SECONDS);
        semaphore = new Semaphore(2);
        this.uiUpdateCallback = uiUpdateCallback;
    }

    public List<IArea> getAvailableAreas() {
        return availableAreas;
    }

    public ObservableList<ThreadInfoModel> getThreadInfoList() {
        return threadInfoList;
    }

    // індивідуальне: щоб потоки завершувалися за 10 сек, обмежити до 2 потоків (спочатку повністю працюють перші
    // 2 потоки, а потім починають наступні 2)
    public void addStation(IArea selectedArea, int interval) {
        int stationId = taskId++;
        ThreadInfoModel threadInfo = new ThreadInfoModel("Thread " + stationId, "Waiting queue",
                "Waiting for result...");
        threadInfoList.add(threadInfo);

        // якщо використовувати семафор перед цим, то не зможемо додавати в чергу поки чекаємо
        ScheduledFuture<?> future = executor.schedule(() -> {
            try {
                semaphore.acquire(); // потоки не будуть висіти, бо в executor все одно максимум 2

                scheduledExecutor.schedule(() -> {
                    removeStation(stationId);
                }, 10, TimeUnit.SECONDS);

                while (!Thread.currentThread().isInterrupted()) {
                    threadInfo.setThreadStatus("Working");
                    Thread.sleep(500); // для відображення статусу Working
                    try {
                        double[][] result = selectedArea.generateData();
                        results.put(stationId, result);
                        String resultText = Arrays.deepToString(result);
                        threadInfo.setThreadResult(resultText);

                        threadInfo.setThreadStatus("Sleeping");

                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        }, 0, TimeUnit.SECONDS);

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
