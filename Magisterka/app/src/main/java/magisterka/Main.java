package magisterka;

import java.sql.*;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class Main {
   

   public static void main(String[] args) 
   {

          // Replace the placeholder with your Atlas connection string
          String uri = "mongodb://localhost:27017/";
          ServerApi serverApi = ServerApi.builder()
                  .version(ServerApiVersion.V1)
                  .build();
          MongoClientSettings settings = MongoClientSettings.builder()
                  .applyConnectionString(new ConnectionString(uri))
                  .serverApi(serverApi)
                  .build();
          try (MongoClient mongoClient = MongoClients.create(settings)) 
        {
              MongoDatabase database = mongoClient.getDatabase("admin");
              try 
                {
                  // Send a ping to confirm a successful connection
                  Bson command = new BsonDocument("ping", new BsonInt64(1));
                  Document commandResult = database.runCommand(command);
                  MongoCollection<Document> collection = database.getCollection("units");

                }
              catch (MongoException me) {
                  System.err.println(me);
              }
                  

        }
    }
}