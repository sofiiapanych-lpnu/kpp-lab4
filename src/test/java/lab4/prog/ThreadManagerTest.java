package lab4.prog;

import javafx.collections.ObservableList;
import lab4.prog.Areas.NorthArea;
import org.junit.jupiter.api.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThreadManagerTest {

    private ThreadManager threadManager;
    private Runnable mockUiUpdateCallback;
    AtomicInteger wasRun;
    @BeforeEach
    void setUp() {
        wasRun = new AtomicInteger(0);

        mockUiUpdateCallback = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                wasRun.incrementAndGet();
            }
        };
        threadManager = new ThreadManager(mockUiUpdateCallback);
    }

    @Test
    void testAddStation() throws InterruptedException {
        NorthArea northArea = new NorthArea();

        threadManager.addStation(northArea, 1);

        ObservableList<ThreadInfoModel> threadInfoList = threadManager.getThreadInfoList();
        assertEquals(1, threadInfoList.size());
        assertEquals("Thread 1", threadInfoList.getFirst().getThreadName().get());

        TimeUnit.SECONDS.sleep(2);

        assertNotNull(threadManager.calculateAverageWeather());
    }

    @Test
    void testRemoveStation() {
        NorthArea northArea = new NorthArea();

        threadManager.addStation(northArea, 1);

        threadManager.removeStation(1);

        ObservableList<ThreadInfoModel> threadInfoList = threadManager.getThreadInfoList();
        assertTrue(threadInfoList.isEmpty());
    }

    @Test
    void testCalculateAverageWeather() {
        NorthArea northArea = new NorthArea();
        double[][] mockData1 = northArea.generateData();
        double[][] mockData2 = northArea.generateData();

        threadManager.results.put(1, mockData1);
        threadManager.results.put(2, mockData2);

        double[][] result = threadManager.calculateAverageWeather();

        assertNotNull(result);
        assertEquals(24, result.length);
    }

    @Test
    void testShutdown() {
        NorthArea northArea = new NorthArea();

        threadManager.addStation(northArea, 1);

        threadManager.shutdown();

        assertTrue(threadManager.executor.isShutdown());
        assertTrue(threadManager.fixedThreadPool.isShutdown());
        assertTrue(threadManager.scheduledExecutor.isShutdown());
    }

    @Test
    void testAddMultipleStations() throws InterruptedException {
        NorthArea northArea = new NorthArea();

        threadManager.addStation(northArea, 1);
        threadManager.addStation(northArea, 1);
        threadManager.addStation(northArea, 1);

        ObservableList<ThreadInfoModel> threadInfoList = threadManager.getThreadInfoList();
        assertEquals(3, threadInfoList.size());
//        assertEquals(0, wasRun.get());
//
//        TimeUnit.SECONDS.sleep(3);
//        assertEquals(3, wasRun.get());
        Thread.sleep(1000);
        assertNotNull(threadManager.calculateAverageWeather());
    }

    @Test
    void testUIUpdaterThread() throws InterruptedException {
        NorthArea northArea = new NorthArea();

        threadManager.addStation(northArea, 1);
        threadManager.addStation(northArea, 1);
        threadManager.addStation(northArea, 1);

        assertEquals(0, wasRun.get());

        TimeUnit.SECONDS.sleep(3);
        assertEquals(1, wasRun.get());
        TimeUnit.SECONDS.sleep(4);
        assertEquals(2, wasRun.get());
    }

    @Test
    void testQueueOverflow() {
        NorthArea northArea = new NorthArea();
        ThreadManager Manager = new ThreadManager(mockUiUpdateCallback);
        for(int i=0; i<7; i++){
            Manager.addStation(northArea, 1);
        }
        ObservableList<ThreadInfoModel> threadInfoList = Manager.getThreadInfoList();
        assertEquals(7, threadInfoList.size());


        assertEquals("Waiting queue", threadInfoList.get(6).getThreadStatus().get());
    }

    @AfterEach
    void tearDown() {
        threadManager.shutdown();
    }
}
