package com.melon.db;

import com.melon.db.codecs.CleanDocumentCodec;
import com.melon.errors.DBException;
import com.melon.utils.LoggerEx;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MongoInstance {
	private static final String TAG = LoggerEx.getClassTag(MongoInstance.class);

	private static Hashtable<String, MongoClient> mongoTable = new Hashtable<>();
	
	private MongoClient mongo;
	private String host;
	private MongoClientOptions options;
	private Integer connectionsPerHost;
	private Integer threadsAllowedToBlockForConnectionMultiplier;
	private Integer maxWaitTime;
	private Integer connectTimeout;
	private Integer socketTimeout;
	private Boolean socketKeepAlive;
	
	
	public synchronized void disconnect() {
		mongo.close();
	}
	
	
	public synchronized void connect() throws DBException{
		if(host == null){
			throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, "Initiate database miss arguments, host = " + host);
		}
		if(mongo == null){
			try {
				initMongoOptions();
				
				final String MONGODB_PROTOCOL = "mongodb://";
				if(host.startsWith(MONGODB_PROTOCOL)) 
					host = host.substring(MONGODB_PROTOCOL.length());
				
				List<ServerAddress> servers = new ArrayList<>();
				String[] hostArray = host.split(",");
				for(String hostString : hostArray) {
					ServerAddress address = new ServerAddress(hostString);
					servers.add(address);
				}
				
				String key = host + ";" + options.toString();
				if(mongoTable.containsKey(key)) {
					LoggerEx.info(TAG, "Reuse last mongo instance as the host, port and options are all the same.");
					mongo = mongoTable.get(key);
				} else {
					mongo = new MongoClient(servers, options);
					mongoTable.put(key, mongo);
					LoggerEx.info(TAG, "New Mongo instance created, " + key);
					
//					DB adminDB = mongo.getDB("admin");
//					boolean bool = adminDB.authenticate("aplomb", "acl123dec@de".toCharArray());
//					AcuLogger.debug(TAG, "MongoDB authorize: " + bool);
				}
			} catch (MongoException e) {
				e.printStackTrace();
				throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, "MongoException 创建数据库对象失败", e.getMessage());
			}
		} else {
//			throw new DBException("数据库已经连接上， 不能重复连接");
		}
//		initDAOMap();
	}
	
	private void initMongoOptions(){
		MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
		if(connectionsPerHost != null)
			optionsBuilder.connectionsPerHost(connectionsPerHost);
		if(threadsAllowedToBlockForConnectionMultiplier != null)
			optionsBuilder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
		if(maxWaitTime != null)
			optionsBuilder.maxWaitTime(maxWaitTime);
		if(connectTimeout != null)
			optionsBuilder.connectTimeout(connectTimeout);
		if(socketTimeout != null)
			optionsBuilder.socketTimeout(socketTimeout);
		if(socketKeepAlive != null)
			optionsBuilder.socketKeepAlive(socketKeepAlive);
		CodecRegistry registry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new CleanDocumentCodec()));
		optionsBuilder.codecRegistry(registry);
		options = optionsBuilder.build();
	}
	
	public MongoClient getMongo() {
		return mongo;
	}
	public void setMongo(MongoClient mongo) {
		this.mongo = mongo;
	}
	public MongoClientOptions getOptions() {
		return options;
	}
	public void setOptions(MongoClientOptions options) {
		this.options = options;
	}
	
	public Integer getConnectionsPerHost() {
		return connectionsPerHost;
	}
	
	public void setConnectionsPerHost(Integer connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}
	
	public Integer getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}
	
	public void setThreadsAllowedToBlockForConnectionMultiplier(
			Integer threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}
	
	public Integer getMaxWaitTime() {
		return maxWaitTime;
	}
	
	public void setMaxWaitTime(Integer maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
	
	public Integer getConnectTimeout() {
		return connectTimeout;
	}
	
	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	public Integer getSocketTimeout() {
		return socketTimeout;
	}
	
	public void setSocketTimeout(Integer socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	
	public Boolean getSocketKeepAlive() {
		return socketKeepAlive;
	}
	
	public void setSocketKeepAlive(Boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
	}
	public String getHost() {
		return host;
	}
	
	public void setHost(String hosts) {
		this.host = hosts;
	}
	
}
