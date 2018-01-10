package rs.pedjaapps.eventlogger.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by apple on 1/9/18.
 */

@Entity
public class BroadcastAction {
    @Id
    @NotNull
    private String action;

    @Generated(hash = 1713179465)
    public BroadcastAction(@NotNull String action) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BroadcastAction that = (BroadcastAction) o;

        return action.equals(that.action);
    }

    @Override
    public int hashCode() {
        return action.hashCode();
    }
}
