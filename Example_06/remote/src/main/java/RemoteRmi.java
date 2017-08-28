import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by panguansen on 17/8/24.
 */
public interface RemoteRmi extends Remote {
    public void sendNoReturn(String message) throws RemoteException, InterruptedException;
    public String sendHasReturn(JoinRmiEvt joinRmiEvt) throws RemoteException;
}
