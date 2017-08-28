import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Created by panguansen on 17/8/24.
 */
public class RemoteRMIServer {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, MalformedURLException, InterruptedException {
        System.out.println("the RemoteRMIServer is Starting ...");
        RemoteRmiImpl remoteRmi = new RemoteRmiImpl();
        System.out.println("Binding server implementation to registry");
        LocateRegistry.createRegistry(2553);
        Naming.bind("rmi://127.0.0.1:2553/remote_rmi",remoteRmi);
        System.out.println("the RemoteRMIServer is Started");
        Thread.sleep(10000000);
    }
}
