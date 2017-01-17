package com.melon.data;

import org.bson.Document;

/**
 * A {@code DBObjetable } is an object that can serialize to {@code DBObject}.
 * @author Aplomb
 *
 */
public interface Documentable extends PersistentObject {
	Document toDocument();

    void fromDocument(Document dbo);
}
