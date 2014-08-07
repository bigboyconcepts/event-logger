package rs.pedjaapps.eventlogger.utility;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SqliteImageLoader extends BaseImageDownloader
{

    private static final String SCHEME_DB = "db";
    private static final String DB_URI_PREFIX = SCHEME_DB + "://";

    public SqliteImageLoader(Context context)
    {
        super(context);
    }

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException
    {
        if (imageUri.startsWith(DB_URI_PREFIX))
        {
            byte[] imageData = (byte[]) extra;
            return new ByteArrayInputStream(imageData);
        }
        else
        {
            return super.getStreamFromOtherSource(imageUri, extra);
        }
    }
}