package rs.pedjaapps.eventlogger.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by apple on 1/9/18.
 */

@Entity
public class BroadcastAction {
    private String action;

    @Generated(hash = 2125234849)
    public BroadcastAction(String action) {
        this.action = action;
    }

    @Generated(hash = 126005551)
    public BroadcastAction() {
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
