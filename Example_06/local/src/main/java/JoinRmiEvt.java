import java.io.Serializable;
import java.rmi.Remote;

/**
 * Created by panguansen on 17/8/24.
 */
public class JoinRmiEvt implements Remote , Serializable{
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;

    public JoinRmiEvt(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
