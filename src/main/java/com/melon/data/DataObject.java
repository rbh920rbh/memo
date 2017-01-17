package com.melon.data;

import com.melon.utils.CommonUtils;
import com.melon.utils.DataInputStreamEx;
import com.melon.utils.DataOutputStreamEx;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public abstract class DataObject implements Documentable {
	public static final String FIELD_ID = "_id";
	
	protected String id;
	
	public DataObject(){
	}

	public boolean isIdGenerated(){
		if(id == null){
			return false;
		}
		return true;
	}
	
	public void generateId(){
//		CRC32 crc = new CRC32();
//		crc.update(UUID.randomUUID().toString().getBytes());
//		id = colName + "_" + crc.getValue();
//		id = colName + "_" + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		if(id == null)
			id = ObjectId.get().toString();
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public Document toJSONObject() {
		return toDocument();
	}
	
	public void persistent(OutputStream os) throws IOException {
		DataOutputStreamEx dos = new DataOutputStreamEx(os);
		dos.writeUTF(id);
	}

	public void resurrect(InputStream is) throws IOException {
		DataInputStreamEx dis = new DataInputStreamEx(is);
		id = dis.readUTF();
	}

	public Document toDocument(){
		Document dbObj1 = new CleanDocument();//TODO need CleanDocument implementation like CleanDBObject
//		Document dbObj1 = new Document();
		if(id != null)
			dbObj1.put(FIELD_ID, id);
		return dbObj1;
	}
	
	public void fromDocument(Document dbObj){
		Object idObj = (Object) dbObj.get(FIELD_ID);
		if(idObj != null) 
			id = idObj.toString();
	}
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataObject other = (DataObject) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    public String toString(){
		return toDocument().toJson(CommonUtils.MONGODB_JSONSETTINGS);
	}
}
