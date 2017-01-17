package com.melon.data;

import org.bson.Document;
import org.bson.types.ObjectId;


public class ResourceData implements Documentable{
	public static final String FIELD_RESOURCE_RESOURCEID = "rid";
	public static final String FIELD_RESOURCE_CONTENTTYPE= "type";
	public static final String FIELD_RESOURCE_NAME = "name";
	private String resourceId;
	private String contentType;
	private String name;
	private Long time;
	
	public ResourceData () {}
	
	public ResourceData(String resourceId, String contentType, String name) {
        setResourceId(resourceId);
        this.contentType = contentType;
        this.name = name;
    }
    public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
		if(resourceId != null) {
			ObjectId oid = new ObjectId(resourceId);
			if(oid != null)
				oid.getDate().getTime();
//			time = ObjectId.massageToObjectId(resourceId).getTime();
		}
	}
	public static void main(String[] args) {
		String id = ObjectId.get().toString();
		System.out.println(id);
		
		ObjectId oid = new ObjectId(id);
//		ObjectId oid = ObjectId.massageToObjectId(id);
		Long time = null;
		if(oid != null)
			time = oid.getDate().getTime();
//			time = oid.getTime();
		System.out.println("oid " + oid + " time " + time);
	}
	@Override
	public Document toDocument() {
		Document dbObj = new CleanDocument();
		dbObj.put(FIELD_RESOURCE_RESOURCEID, resourceId);
		dbObj.put(FIELD_RESOURCE_NAME, name);
		dbObj.put(FIELD_RESOURCE_CONTENTTYPE, contentType);
		return dbObj;
	}
	
	@Override
	public void fromDocument(Document dbObj) {
		resourceId = (String) dbObj.get(FIELD_RESOURCE_RESOURCEID);
		contentType = (String) dbObj.get(FIELD_RESOURCE_CONTENTTYPE);
		name = (String) dbObj.get(FIELD_RESOURCE_NAME);
		ObjectId oid = new ObjectId(resourceId);
		if(oid != null) 
			time = oid.getDate().getTime();
//		time = ObjectId.massageToObjectId(resourceId).getTime();
	}


    @Override
    public String toString() {
        return "Resource [resourceId=" + resourceId + ", contentType=" + contentType + ", name=" + name + ", time=" + time + "]";
    }

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}
	
}
