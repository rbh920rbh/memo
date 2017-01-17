package com.melon.db;

import com.melon.errors.DBException;
import com.mongodb.DB;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import org.bson.Document;

public class MongoHelper {

	private MongoInstance mongo;
	private MongoDatabase db;
	private String dbName;
	private DB dbForGridFS;
	
	private Long cappedSize = 1 * 1024 * 1024 * 1024L;  //default is 1G 
	private Long cappedMax = 1 * 1000 * 1000L; //default is 1000000
	
	public MongoHelper(){
	}
	
	
	public synchronized void init() throws DBException{
		db = mongo.getMongo().getDatabase(dbName);
		dbForGridFS = mongo.getMongo().getDB(dbName);
	}
	
	public String clearDB(){
		String dbName = db.getName();
		db.drop();
		return dbName;
	}
	
	public MongoCollection<Document> getDBCollection(String colName){
		MongoCollection<Document> col = db.getCollection(colName);
		return col;
	}
	
	public MongoCollection<Document> getCappedDBCollection(String colName){
		return getCappedDBCollection(colName, true);
	}
	
	public MongoCollection<Document> getCappedDBCollection(String colName, boolean create){
		MongoCollection<Document> col = db.getCollection(colName);
		if(col != null) {
			if(create) {
				CreateCollectionOptions options = new CreateCollectionOptions();
				options.capped(true);
				options.maxDocuments(cappedMax);
				options.sizeInBytes(cappedSize);
				options.autoIndex(false);
//				final BasicDBObject options = new BasicDBObject("capped", true);
//				options.put("size",  cappedSize); 
//				options.put("max",  cappedMax); 
//				options.put("autoIndexId",  false); 
				db.createCollection(colName, options);
				col = db.getCollection(colName);
			}
		} 
		return col;
	}
	/**
	 * @param dbName the dbName to set
	 */
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the dbName
	 */
	
	public String getDbName() {
		return dbName;
	}

	/**
	 * @return the db
	 */
	public MongoDatabase getDb() {
		return db;
	}

	/**
	 * @param mongo the mongo to set
	 */
	
	public void setMongo(MongoInstance mongo) {
		this.mongo = mongo;
	}

	/**
	 * @return the mongo
	 */
	
	public MongoInstance getMongo() {
		return mongo;
	}

	public Long getCappedSize() {
		return cappedSize;
	}

	public void setCappedSize(Long cappedSize) {
		this.cappedSize = cappedSize;
	}

	public Long getCappedMax() {
		return cappedMax;
	}

	public void setCappedMax(Long cappedMax) {
		this.cappedMax = cappedMax;
	}

	public DB getDbForGridFS() {
		return dbForGridFS;
	}

	public void setDbForGridFS(DB dbForGridFS) {
		this.dbForGridFS = dbForGridFS;
	}
	
}
