package futures;

import java.util.concurrent.*;

/**
 * Created by panguansen on 17/5/14.
 */
public class Test {
    public static void main(String[] args) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future f = es.submit(() -> {
                System.out.println("execute call");
                Thread.sleep(1000);
                return 5;
            });
        try {
            System.out.println(f.isDone());
            System.out.println(f.get(2000, TimeUnit.MILLISECONDS));
            System.out.println(f.isDone());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
