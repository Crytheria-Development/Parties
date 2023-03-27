package me.byteful.plugin.parties.api.data.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import me.byteful.plugin.parties.api.data.DataManager;
import me.byteful.plugin.parties.data.parties.Party;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.io.IOException;
import java.util.UUID;

public class MongoDataManager implements DataManager {
  private static final JsonWriterSettings SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build();
  private final Gson gson;
  private final MongoDatabase mongo;
  private final MongoClient client;

  public MongoDataManager(String mongoURI, String database) {
    if (mongoURI.isBlank() || database.isBlank()) {
      throw new RuntimeException("Please configure MongoDB connection information in the config!");
    }
    this.gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    this.client = new MongoClient(new MongoClientURI(mongoURI));
    this.mongo = this.client.getDatabase(database);
  }

  private void set(String collection, String id, Object object) {
    final Bson filter = Filters.eq("_id", id);
    final MongoCollection<Document> coll = mongo.getCollection(collection);
    final Document doc = Document.parse(gson.toJson(object)).append("_id", id);
    if (coll.find(filter).first() != null) {
      coll.replaceOne(filter, doc);
    } else {
      coll.insertOne(doc);
    }
  }

  private <T> T get(String collection, String id, Class<T> type) {
    final Bson filter = Filters.eq("_id", id);
    final MongoCollection<Document> coll = mongo.getCollection(collection);
    final Document found = coll.find(filter).first();
    if (found == null) {
      return null;
    }
    final String json = found.toJson(SETTINGS);
    return gson.fromJson(json, type);
  }

  @Override
  public Party getParty(UUID member) {
    final MongoCollection<Document> coll = mongo.getCollection("parties");
    for (Document document : coll.find()) {
      if (document == null) {
        continue;
      }

      final String json = document.toJson(SETTINGS);
      final Party party = gson.fromJson(json, Party.class);
      if (party.isPlayerInParty(member)) {
        return party;
      }
    }

    return null;
  }

  @Override
  public void setParty(Party party) {
    set("parties", party.getUniqueId().toString(), party);
  }

  @Override
  public void removeParty(Party party) {
    final MongoCollection<Document> parties = mongo.getCollection("parties");
    parties.deleteOne(Filters.eq("_id", party.getUniqueId().toString()));
  }

  @Override
  public void close() throws IOException {
    try {
      client.close();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }
}
