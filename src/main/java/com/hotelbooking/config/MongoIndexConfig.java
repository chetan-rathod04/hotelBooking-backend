package com.hotelbooking.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class MongoIndexConfig implements CommandLineRunner {

    @Autowired
    private MongoClient mongoClient;

    @Override
    public void run(String... args) {
        // Connect to the correct database
        MongoDatabase database = mongoClient.getDatabase("Hotel_booking");

        // Get the 'rooms' collection
        MongoCollection<Document> roomsCollection = database.getCollection("rooms");

        // Create a unique index on the 'roomNumber' field
        roomsCollection.createIndex(
            new Document("roomNumber", 1), // ascending order
            new IndexOptions().unique(true) // make it unique
        );

        System.out.println("✅ Unique index created on 'roomNumber' field.");
    }
}
