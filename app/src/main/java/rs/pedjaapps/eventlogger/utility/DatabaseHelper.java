package rs.pedjaapps.eventlogger.utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;

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
            db.execSQL("ALTER TABLE " + EventDao.TABLENAME + " ADD COLUMN icon_id INTEGER");
            IconDao.createTable(db, true);
        }
    }
}
