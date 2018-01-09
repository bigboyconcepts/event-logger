package com.newtecsolutions.floorball.utils;

import com.newtecsolutions.floorball.model.CampTrainer;
import com.newtecsolutions.floorball.model.Club;
import com.newtecsolutions.floorball.model.FCMRegistrationId;
import com.newtecsolutions.floorball.model.File;
import com.newtecsolutions.floorball.model.Log;
import com.newtecsolutions.floorball.model.Mail;
import com.newtecsolutions.floorball.model.Member;
import com.newtecsolutions.floorball.model.Notification;
import com.newtecsolutions.floorball.model.OAuthAccessToken;
import com.newtecsolutions.floorball.model.OAuthClient;
import com.newtecsolutions.floorball.model.OAuthRefreshToken;
import com.newtecsolutions.floorball.model.RegistrationCode;
import com.newtecsolutions.floorball.model.Role;
import com.newtecsolutions.floorball.model.RolePermissionRole;
import com.newtecsolutions.floorball.model.Track;
import com.newtecsolutions.floorball.model.TrackActionRegion;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.exception.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


public class HibernateUtil
{
    private static final SessionFactory sessionFactory;

    static
    {
        try
        {
            // Create the SessionFactory

            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();

            // Hibernate settings equivalent to hibernate.cfg.xml's properties
            Map<String, String> settings = new HashMap<>();
            settings.put(Environment.DRIVER, ConfigManager.getInstance().getString(ConfigManager.CONFIG_DB_DRIVER_CLASS, "com.mysql.jdbc.Driver"));
            settings.put(Environment.URL, ConfigManager.getInstance().getString(ConfigManager.CONFIG_DB_URL, "jdbc:mysql://localhost/floorball"));
            settings.put(Environment.USER, ConfigManager.getInstance().getString(ConfigManager.CONFIG_DB_USERNAME, "root"));
            settings.put(Environment.PASS, ConfigManager.getInstance().getString(ConfigManager.CONFIG_DB_PASSWORD, ""));
            settings.put(Environment.DIALECT, ConfigManager.getInstance().getString(ConfigManager.CONFIG_DB_DIALECT, "org.hibernate.dialect.MySQLDialect"));
            settings.put(Environment.SHOW_SQL, String.valueOf(ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_DB_SHOW_SQL, false)));
            settings.put(Environment.FORMAT_SQL, String.valueOf(ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_DB_FORMAT_SQL, false)));
            settings.put(Environment.HBM2DDL_AUTO, ConfigManager.getInstance().getString(ConfigManager.CONFIG_DB_HBMDDL_AUTO, "update"));
            settings.put("hibernate.connection.useUnicode", "true");
            settings.put("hibernate.connection.characterEncoding", "UTF-8");
            settings.put("hibernate.connection.autoReconnect", "true");
            settings.put("hibernate.connection.autoReconnectForPools", "true");

            //c3p0
            settings.put("connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider");
            settings.put("hibernate.c3p0.min_size", "5");
            settings.put("hibernate.c3p0.max_size", "20");
            settings.put("hibernate.c3p0.timeout", "300");
            settings.put("hibernate.c3p0.max_statements", "50");
            settings.put("hibernate.c3p0.idle_test_period", "3000");

            settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
            settings.put("hibernate.jdbc.batch_size", "30");

            // Apply settings
            registryBuilder.applySettings(settings);

            // Create registry
            StandardServiceRegistry registry = registryBuilder.build();

            // Create MetadataSources
            MetadataSources sources = new MetadataSources(registry);

            //add annotated classes
            sources.addAnnotatedClass(Member.class);
            sources.addAnnotatedClass(OAuthRefreshToken.class);
            sources.addAnnotatedClass(OAuthClient.class);
            sources.addAnnotatedClass(OAuthAccessToken.class);
            sources.addAnnotatedClass(Mail.class);
            sources.addAnnotatedClass(Log.class);
            sources.addAnnotatedClass(Role.class);
            sources.addAnnotatedClass(RolePermissionRole.class);
            sources.addAnnotatedClass(Club.class);
            sources.addAnnotatedClass(RegistrationCode.class);
            sources.addAnnotatedClass(Track.class);
            sources.addAnnotatedClass(File.class);
            sources.addAnnotatedClass(TrackActionRegion.class);
            sources.addAnnotatedClass(FCMRegistrationId.class);
            sources.addAnnotatedClass(Notification.class);
            sources.addAnnotatedClass(CampTrainer.class);

            Metadata metadata = sources.getMetadataBuilder().build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
        }
        catch (Throwable ex)
        {
            // Make sure you log the exception, as it might be swallowed  
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    /**
     * Commit transaction ignoring constraint violation*/
    public static void commitTransactionIgnoringConstraintViolation(Transaction transaction)
    {
        try
        {
            transaction.commit();
        }
        catch (Exception e)
        {
            LogUtils.getLogger().log(Level.INFO, e.getMessage(), e);
            if(!(e instanceof ConstraintViolationException) && !(e.getCause() instanceof ConstraintViolationException))
                throw new RuntimeException(e);
            //photo viewed already
        }
    }

    /**
     * Check if hibernate model has field*/
    public static boolean modelHasField(Class<?> modelClass, String field)
    {
        try
        {
            HibernateUtil.getSessionFactory().getMetamodel().entity(modelClass).getAttribute(field);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        return true;
    }
}