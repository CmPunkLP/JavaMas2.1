import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class Main {
    private static int[] array;
    private static int currentLength;
    private static int numberOfThreads;
    private static CountDownLatch latch;
    private static AtomicInteger completedPairs = new AtomicInteger(0);
    private static final Object lockObject = new Object();

    public static void main(String[] args) {
        Random rand = new Random();
        currentLength = 500000;
        array = rand.ints(currentLength, 1, 10).toArray(); // Заповнюємо масив випадковими числами від 1 до 9

        System.out.println("Початковий масив: " + Arrays.toString(array));

        numberOfThreads = Runtime.getRuntime().availableProcessors(); 
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        while (currentLength > 1) {
            completedPairs.set(0);
            int pairsToProcess = currentLength / 2;
            latch = new CountDownLatch(numberOfThreads);
            int pairsPerThread = pairsToProcess / numberOfThreads;

            for (int i = 0; i < numberOfThreads; i++) {
                int startPair = i * pairsPerThread;
                int endPair = (i == numberOfThreads - 1) ? pairsToProcess : startPair + pairsPerThread;
                executorService.submit(() -> Worker(startPair, endPair));
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            currentLength = pairsToProcess;
            System.out.println("Проміжний масив: " + Arrays.toString(Arrays.copyOf(array, currentLength)));
        }

        executorService.shutdown();
        System.out.println("Фінальна сума: " + array[0]);
    }

    private static void Worker(int startPair, int endPair) {
        for (int i = startPair; i < endPair; i++) {
            int index1 = i;
            int index2 = currentLength - 1 - i;

            int sum = array[index1] + array[index2];
            array[index1] = sum;

            synchronized (lockObject) {
                completedPairs.incrementAndGet();
                if (completedPairs.get() == (currentLength / 2)) {
                    latch.countDown();
                }
            }
        }
        latch.countDown();
    }
}
