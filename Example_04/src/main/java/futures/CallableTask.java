package futures;

import java.util.concurrent.Callable;

/**
 * Created by panguansen on 17/5/14.
 */
public class CallableTask implements Callable{
    @Override
    public Object call() throws Exception {
        System.out.println("lalala");
        Thread.sleep(1000);
        throw new Exception("error");
//        return "hello world";
    }
}
