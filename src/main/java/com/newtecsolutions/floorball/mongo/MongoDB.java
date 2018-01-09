package com.newtecsolutions.floorball.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.newtecsolutions.floorball.utils.ConfigManager;

import org.bson.Document;

import java.util.Collections;

/**
 * Created by pedja on 7/14/17 2:33 PM.
 * This class is part of the FloorBallBackend
 * Copyright © 2017 ${OWNER}
 */
public class MongoDB
{
    private static MongoDB instance;

    public synchronized static MongoDB getInstance()
    {
        if(instance == null)
            instance = new MongoDB();
        return instance;
    }

    private MongoDatabase database;

    private MongoDB()
    {
        boolean authenticate = ConfigManager.getInstance().getBoolean(ConfigManager.CONFIG_MONGO_AUTH, true);
        MongoClient mongoClient;
        if(authenticate)
        {
            MongoCredential credential = MongoCredential.createCredential(
                    ConfigManager.getInstance().getString(ConfigManager.CONFIG_MONGO_USERNAME, "floorball"),
                    ConfigManager.getInstance().getString(ConfigManager.CONFIG_MONGO_DBNAME, "floorball"),
                    ConfigManager.getInstance().getString(ConfigManager.CONFIG_MONGO_PASSWORD, "password").toCharArray());
            mongoClient = new MongoClient(new ServerAddress(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MONGO_HOSTNAME, "localhost"), ConfigManager.getInstance().getInt(ConfigManager.CONFIG_MONGO_PORT, 27017)), Collections.singletonList(credential));
        }
        else
        {
            mongoClient = new MongoClient(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MONGO_HOSTNAME, "localhost"), ConfigManager.getInstance().getInt(ConfigManager.CONFIG_MONGO_PORT, 27017));
        }
        database = mongoClient.getDatabase(ConfigManager.getInstance().getString(ConfigManager.CONFIG_MONGO_DBNAME, "floorball"));
    }

    public void insert(Document document, String collectionName)
    {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
    }
}
