package com.melon.db;

import com.melon.data.DataObject;
import com.melon.errors.DBException;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

import java.util.Iterator;
import java.util.LinkedList;



/**
 * FIXME Not finished.
 * 
 * @author chenzhuo
 *
 */
public class MongoTransaction {
	private boolean isCommited = false;
	private MongoHelper helper;
	private LinkedList<DataObjectAction> list = new LinkedList<DataObjectAction>();;
	
	public MongoTransaction(){
	}
	
	public synchronized void save(DataObject dObj) {
		if(isCommited){
			System.out.println("DataObject has already commited. can NOT commit again!");
			return;
		}
		if(!dObj.isIdGenerated()){
			dObj.generateId();
//				col.insert(dataObject.toDocument());
			list.add(new DataObjectAction(DataObjectAction.INSERT, dObj));
		}else{
//				String id = dObj.getId();
//				BasicDBObject query = new BasicDBObject();
//				query.put("id", id);
//				col.update(query, dataObject.toDocument());
			list.add(new DataObjectAction(DataObjectAction.UPDATE, dObj));
		}
	}
	
	/**
	 * TODO could implement the rollback function.
	 * 
	 * @throws DBException
	 */
	public synchronized void commit() throws DBException {
		Iterator<DataObjectAction> iter = list.listIterator();
//		while (iter.hasNext()) {
//			DataObjectAction dObjAct = iter.next();
//			DAO dao = helper.getDAO((Class<? extends DataObject>) dObjAct.params[0]);
//			switch (dObjAct.action) {
//			case DataObjectAction.INSERT:
//				dao.add((DataObject) dObjAct.params[1]);
//				break;
//			case DataObjectAction.UPDATE:
//				dao.update((DBObject)dObjAct.params[1], (DBObject)dObjAct.params[2]);
//				break;
//			case DataObjectAction.DELETE:
//				dao.delete((DataObject)dObjAct.params[1]);
//				break;
//			}
//		}
		list.clear();
	}
	
	class DataObjectAction{
		static final int DELETE = 0;
		static final int UPDATE = 1;
		static final int INSERT = 2;
		Object[] params;
		int action;
		
		public DataObjectAction(int type, Object...data) {
			action = type;
			params = data;
		}
		
	}

	public void add(Class<? extends DataObject> clazz, DataObject dObj) throws MongoException, DBException{
		check();
		dObj.generateId();
		list.add(new DataObjectAction(DataObjectAction.INSERT, clazz, dObj));
	}

	public void delete(Class<? extends DataObject> clazz, DataObject match) throws DBException {
		check();
		list.add(new DataObjectAction(DataObjectAction.DELETE, clazz, match));
	}

	public void update(Class<? extends DataObject> clazz, DBObject match, DBObject dObj) throws DBException {
		check();
		list.add(new DataObjectAction(DataObjectAction.UPDATE, clazz, match, dObj));
	}
	
	private void check() throws DBException{
		if(isCommited){
			throw new DBException(DBException.ERRORTYPE_UNKNOWN, 0, "DataObject has already commited. can NOT commit again!");
		}
	}
}
