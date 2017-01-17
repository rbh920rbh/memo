package com.melon.db.codecs;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class IntegerCodec implements Codec<Integer> {
    @Override
    public void encode(final BsonWriter writer, final Integer value, final EncoderContext encoderContext) {
        writer.writeInt32(value);
    }

    @Override
    public Integer decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readInt32();
    }

    @Override
    public Class<Integer> getEncoderClass() {
        return Integer.class;
    }
    
    public static void main(String[] args) {
		System.out.println(String[].class);
		System.out.println(Iterable.class);
	}
}