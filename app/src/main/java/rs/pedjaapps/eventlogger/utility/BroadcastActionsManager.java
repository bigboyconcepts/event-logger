package rs.pedjaapps.eventlogger.utility;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rs.pedjaapps.eventlogger.App;
import rs.pedjaapps.eventlogger.R;
import rs.pedjaapps.eventlogger.constants.Constants;
import rs.pedjaapps.eventlogger.model.BroadcastAction;

/**
 * Created by apple on 1/10/18.
 */

public class BroadcastActionsManager {
    private static final BroadcastActionsManager instance = new BroadcastActionsManager();

    public static BroadcastActionsManager getInstance() {
        return instance;
    }

    public void importBundledActions()
    {
        String[] actionStrings = Utility.readRawFile(R.raw.broadcast_actions).split("\n");
        List<BroadcastAction> actions = new ArrayList<>();

        for(String as : actionStrings)
        {
            actions.add(new BroadcastAction(as));
        }
        App.getInstance().getDaoSession().getBroadcastActionDao().insertOrReplaceInTx(actions);
    }

    public Set<BroadcastAction> importDumpsysActions()
    {
        Set<BroadcastAction> actions = new HashSet<>(App.getInstance().getDaoSession().getBroadcastActionDao().loadAll());
        try
        {
            Process p = Runtime.getRuntime().exec("dumpsys activity b");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));

            Set<BroadcastAction> actionsDumpsys = new HashSet<>();

            String line;
            while ((line = r.readLine()) != null)
            {
                if (line.startsWith("      Action:"))
                {
                    line = line.substring(15, line.length() - 1);
                    actionsDumpsys.add(new BroadcastAction(line));
                }
            }
            actions.addAll(actionsDumpsys);
        }
        catch (IOException e)
        {
            Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        return actions;
    }

}
