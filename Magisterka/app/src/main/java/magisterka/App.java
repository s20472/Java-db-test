/*
 * This source file was generated by the Gradle 'init' task
 */
package magisterka;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
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

import redis.clients.jedis.UnifiedJedis;

public class App 
{
    

    public static void main(String[] args) 
    {
    HashMap<String,Long> times = new HashMap<String,Long>();
    Random rnd = new Random();
    Timer timer = new Timer();

    
    
    // start creating dataset
    List<Record> records = new ArrayList<Record>();
    List<Record> result = new ArrayList<Record>();
    String symbols = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
    for(long i =0;i<1000000;i++)
    {
        StringBuilder string1= new StringBuilder();
        StringBuilder string2= new StringBuilder();
        while(string1.length()<35)
        {
            string1.append(symbols.charAt((int)rnd.nextInt(symbols.length())));
            string2.append(symbols.charAt((int)rnd.nextInt(symbols.length())));
        }
        records.add(new Record(string1.toString(),string2.toString(),rnd.nextInt(20000),rnd.nextInt(20000)));
        if(i%100000==0)
        {
            //System.out.println(i);
        }
    }

    
    //end creating data set


    
    //start redis test
    result.clear();
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6379");
        jedis.flushAll();
        timer.Start();
        for(int i =0;i<records.size();i++)
        {
            jedis.set("str1:"+i,records.get(i).str1);
            jedis.set("str2:"+i,records.get(i).str2);
            jedis.set("num1:"+i,""+records.get(i).num1);
            jedis.set("num2:"+i,""+records.get(i).num2);
            if(i%100000==0)
        {
            //System.out.println("redis "+i);
        }
        }
        times.put("redis_insert", timer.End());
        
        timer.Start();
        for(int i =0;i<records.size();i++)
        {
            jedis.set("num2:"+i,"2137");
        }
        times.put("redis_update", timer.End());
        
        timer.Start();
        for(int i =0;i<records.size();i++)
        {
            System.out.println(i);
            result.add(new Record(
            (String)jedis.get("str1:"+i),
            (String)jedis.get("str2:"+i),
            Integer.parseInt(jedis.get("num1:"+i)),
            Integer.parseInt(jedis.get("num2:"+i))
            ));
        }
        times.put("redis_select", timer.End());

        jedis.close();
        
        /*
        String res1 = jedis.set("bike:1", "Deimos");
        System.out.println(res1); // OK

        String res2 = jedis.get("bike:1");
        System.out.println(res2);
        */

    //end redis test

    
    //start mongo test
    result.clear();
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
                MongoCollection<Document> collection = database.getCollection("stress_test");
                collection.deleteMany(new Document()); //clear collection


                timer.Start();
                        for(int i =0;i<records.size();i++)
                {
                    collection.insertOne(new Document()
                    .append("str1",records.get(i).str1)
                    .append("str2",records.get(i).str2)
                    .append("num1",records.get(i).num1)
                    .append("num2",records.get(i).num2)
                    );


                    if(i%100000==0)
                    {
                        //System.out.println("mongo "+i);
                    }
                }
                times.put("mongo_insert", timer.End());
                timer.Start();
                collection.updateMany(new Document(),new Document("$set",new Document("num2",2137)));
                times.put("mongo_update", timer.End());
                timer.Start();
                List<Document> tempList = collection.find().into(new ArrayList<Document>());
                for(int i=0;i<tempList.size();i++)
                {
                    result.add(new Record(
                    (String)tempList.get(i).get("str1"),
                    (String)tempList.get(i).get("str2"),
                    (int)tempList.get(i).get("num1"),
                    (int)tempList.get(i).get("num2")
                     ));
                }
                times.put("mongo_select", timer.End());
                

                collection.insertOne(new Document());
            }
            catch (MongoException me) {
                System.err.println(me);
            }
                

    }
    //end mongo test

    
    //start postgres test
    result.clear();
    try {
        
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stress_test","postgres","Traktormamy7");
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("delete from test");

        timer.Start();
            for(int i =0;i<records.size();i++)
        {
            
            stmt.executeUpdate("insert into test (str1,str2,int1,int2) values ('" + 
            records.get(i).str1 + "', '" +
            records.get(i).str2 + "', " +
            records.get(i).num1 + ", " +
            records.get(i).num2 +
            ")");

            if(i%100000==0)
            {
                //System.out.println("postgres "+i);
            }
        }
        times.put("postgres_insert", timer.End());
        timer.Start();
        stmt.executeUpdate("UPDATE test set int1 = 2137");
        times.put("postgres_update", timer.End());
        timer.Start();
        ResultSet rs = stmt.executeQuery("SELECT * from test");
        while (rs.next()) 
        {
           // Retrieve by column name
           result.add(
            new Record(
                rs.getString("str1"),
                rs.getString("str2"),
                rs.getInt("int1"),
                rs.getInt("int2")
            )
           );
        }
        times.put("postgres_select", timer.End());
        /* 
        
        ResultSet rs = stmt.executeQuery("SELECT * from test");
         
            
         // Extract data from result set
         while (rs.next()) 
         {
            // Retrieve by column name
            System.out.print("Str1: " + rs.getString("str1"));
            System.out.print(", Str2: " + rs.getString("str2"));
         }
      */
    } catch (SQLException e) {
        e.printStackTrace();
    }

    //end postgres test
/* 
    System.out.println("Times:");
    System.out.println("Postgres insert:" + times.get("postgres_insert"));
    System.out.println("Postgres update:" + times.get("postgres_update"));
    System.out.println("Postgres select:" + times.get("postgres_select"));
    System.out.println("MongoDB insert:" + times.get("mongo_insert"));
    System.out.println("MongoDB update:" + times.get("mongo_update"));
    System.out.println("MongoDB select:" + times.get("mongo_select"));
    System.out.println("Redis insert:" + times.get("redis_insert"));
    System.out.println("Redis update:" + times.get("redis_update"));
    System.out.println("Redis select:" + times.get("redis_select"));
    */
    System.out.println("Times:");
    System.out.println(times.get("postgres_insert"));
    System.out.println(times.get("postgres_update"));
    System.out.println(times.get("postgres_select"));
    System.out.println(times.get("mongo_insert"));
    System.out.println(times.get("mongo_update"));
    System.out.println(times.get("mongo_select"));
    System.out.println(times.get("redis_insert"));
    System.out.println(times.get("redis_update"));
    System.out.println(times.get("redis_select"));
    }
}