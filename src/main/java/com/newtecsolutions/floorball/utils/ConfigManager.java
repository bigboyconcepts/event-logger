package com.newtecsolutions.floorball.utils;


import org.apache.commons.io.FileUtils;
import org.skynetsoftware.jutils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pedja on 9.11.15. 12.07.
 * This class is part of the p-net
 * Copyright © 2015 ${OWNER}
 *
 */
public class ConfigManager
{
    public static final String CONFIG_LOG_LEVEL_NAME = "log.level";

    public static final String CONFIG_OAUTH_ACCESS_TOKEN_EXPIRES_NAME = "oauth.access_token_expires_millis";
    public static final String CONFIG_OAUTH_REFRESH_TOKEN_EXPIRES_NAME = "oauth.refresh_token_expires_millis";

    public static final String CONFIG_MAIL_PROVIDER_CLASS_NAME = "mail.provider_class_name";
    public static final String CONFIG_MAIL_WORKER_THREAD_COUNT = "mail.worker_thread_count";
    public static final String CONFIG_MAIL_SMTP_HOST = "mail.smtp_host";
    public static final String CONFIG_MAIL_SMTP_USERNAME = "mail.smtp_username";
    public static final String CONFIG_MAIL_SMTP_PASSWORD = "mail.smtp_password";
    public static final String CONFIG_MAIL_FROM = "mail.from";
    public static final String CONFIG_MAIL_SMTP_PORT = "mail.smtp_port";
    public static final String CONFIG_MAIL_SMTP_AUTH = "mail.smtp_auth";
    public static final String CONFIG_MAIL_SMTP_STARTTLS_ENABLED = "mail.smtp_starttls_enabled";
    public static final String CONFIG_MAIL_SMTP_SSL_TRUST = "mail.smpt_ssl_trust";
    public static final String CONFIG_MAIL_DEV_ERR_MAIL = "mail.dev_error_mail";
    public static final String CONFIG_MAIL_QUEUE_ADD_FROM_DB = "mail.queue.add_from_db";
    public static final String CONFIG_MAIL_SEND_ERROR_REPORT = "mail.send_error_reports";
    public static final String CONFIG_MAIL_SMTP_SSL_ENABLED = "mail.smtp_ssl_enabled";

    public static final String CONFIG_DB_DIALECT = "db.dialect";
    public static final String CONFIG_DB_DRIVER_CLASS = "db.connection_driver_class";
    public static final String CONFIG_DB_URL = "db.connection_url";
    public static final String CONFIG_DB_USERNAME = "db.connection_username";
    public static final String CONFIG_DB_PASSWORD = "db.connection_password";
    public static final String CONFIG_DB_SHOW_SQL = "db.show_sql";
    public static final String CONFIG_DB_FORMAT_SQL = "db.format_sql";
    public static final String CONFIG_DB_HBMDDL_AUTO = "db.hbm2ddl_auto";

    public static final String CONFIG_DB_QUERY_DEF_PER_PAGE = "db.query.def_per_page";
    public static final String CONFIG_DB_QUERY_MAX_PER_PAGE = "db.query.max_per_page";

    public static final String CONFIG_MONGO_HOSTNAME = "mongo.hostname";
    public static final String CONFIG_MONGO_PORT = "mongo.port";
    public static final String CONFIG_MONGO_DBNAME = "mongo.dbname";
    public static final String CONFIG_MONGO_AUTH = "mongo.auth";
    public static final String CONFIG_MONGO_USERNAME = "mongo.username";
    public static final String CONFIG_MONGO_PASSWORD = "mongo.password";

    public static final String CONFIG_SERVER_PORT = "server.port";
    public static final String CONFIG_SERVER_STATIC_CONTENT_DIR = "server.static_content_dir";

    public static final String CONFIG_AM_MAX_ENTRIES = "am.max_entries";

    public static final String CONFIG_SOCKETIO_PORT = "socketio.port";
    public static final String CONFIG_FCM_API_KEY = "fcm.api_key";
    public static final String CONFIG_FCM_PROJECT_ID = "fcm.project_id";
    public static final String CONFIG_FCM_DEBUG = "fcm.debug";

    public static final String CONFIG_SSL_ENABLE = "ssl.enabled";
    public static final String CONFIG_SSL_KEYSTORE_PATH = "ssl.keystore_path";
    public static final String CONFIG_SSL_KEYSTORE_PASSWORD = "ssl.keystore_password";

    private static final Pattern KEY_VALUE_ROW_PATTER = Pattern.compile("(.+?)=(.+)");

    private static final String CONFIG_FILENAME = "floorball.conf";
    private static final File CONFIG_FILE;
    private static final File CONFIG_FOLDER;

    static
    {
        //init config file
        CONFIG_FOLDER = new File(System.getProperty("user.home"), "floorball");
        CONFIG_FOLDER.mkdirs();
        CONFIG_FILE = new File(CONFIG_FOLDER, CONFIG_FILENAME);
        LogUtils.info("user.home: " + System.getProperty("user.home"));
    }

    private Map<String, String> mConfigMap;

    private static ConfigManager instance;

    public static synchronized ConfigManager getInstance()
    {
        if(instance == null)
        {
            instance = new ConfigManager();
        }
        return instance;
    }

    private ConfigManager()
    {
        if(!CONFIG_FILE.exists())
        {
            mConfigMap = new HashMap<>();
        }
        else
        {
            mConfigMap = readConfigFromFile(CONFIG_FILE);
        }
    }

    /**
     * <pre>
     * Read config from file
     * config file is simple key value map: key=value
     * </pre>*/
    private Map<String, String> readConfigFromFile(File file)
    {
        Map<String, String> map = new HashMap<>();
        try
        {
            List<String> lines = FileUtils.readLines(file);
            for(String line : lines)
            {
                Matcher matcher = KEY_VALUE_ROW_PATTER.matcher(line);
                if(!matcher.matches())
                    continue;
                String key = matcher.group(1).trim();
                String value = matcher.group(2);
                if(value != null)
                    value= value.trim();
                map.put(key, value);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * Get integer value from config for key
     * @param key returns value for this key
     * @param fallback value to return in case that key doesn't exist or is not int*/
    public int getInt(String key, int fallback)
    {
        try
        {
            return Integer.parseInt(mConfigMap.get(key));
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    /**
     * Get long value from config for key
     * @param key returns value for this key
     * @param fallback value to return in case that key doesn't exist or is not long*/
    public long getLong(String key, long fallback)
    {
        try
        {
            return Long.parseLong(mConfigMap.get(key));
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    /**
     * Get float value from config for key
     * @param key returns value for this key
     * @param fallback value to return in case that key doesn't exist or is not float*/
    public float getFloat(String key, float fallback)
    {
        try
        {
            return Float.parseFloat(mConfigMap.get(key));
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    /**
     * Get double value from config for key
     * @param key returns value for this key
     * @param fallback value to return in case that key doesn't exist or is not double*/
    public double getDouble(String key, double fallback)
    {
        try
        {
            return Double.parseDouble(mConfigMap.get(key));
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    /**
     * Get boolean value from config for key
     * @param key returns value for this key
     * @param fallback value to return in case that key doesn't exist or is not boolean*/
    public boolean getBoolean(String key, boolean fallback)
    {
        try
        {
            return Boolean.parseBoolean(mConfigMap.get(key));
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    /**
     * Get String value from config for key
     * @param key returns value for this key
     * @return value or null*/
    public String getString(String key)
    {
        return mConfigMap.get(key);
    }

    /**
     * Get String value from config for key
     * @param key returns value for this key
     * @param fallback value to return in case that key doesn't exist (is null)*/
    public String getString(String key, String fallback)
    {
        String value = mConfigMap.get(key);
        if(StringUtils.isEmpty(value))
            return fallback;
        return value;
    }
}
