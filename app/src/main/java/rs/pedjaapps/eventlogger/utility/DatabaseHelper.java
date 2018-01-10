package rs.pedjaapps.eventlogger.utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

import rs.pedjaapps.eventlogger.model.BroadcastAction;
import rs.pedjaapps.eventlogger.model.BroadcastActionDao;
import rs.pedjaapps.eventlogger.model.DaoMaster;
import rs.pedjaapps.eventlogger.model.EventDao;
import rs.pedjaapps.eventlogger.model.IconDao;

/**
 * Created by pedja on 27.12.15..
 */
public class DatabaseHelper extends DaoMaster.OpenHelper
{
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory)
    {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion)
    {
        if(oldVersion == 3)
        {
            upgradeTo4(db);
            upgradeTo5(db);
        }
        if(oldVersion == 4)
        {
            upgradeTo5(db);
        }
    }

    private void upgradeTo5(Database db) {
        BroadcastActionDao.createTable(db, true);
    }

    private void upgradeTo4(Database db) {
        db.execSQL("ALTER TABLE " + EventDao.TABLENAME + " ADD COLUMN icon_id INTEGER");
        IconDao.createTable(db, true);
    }
}
