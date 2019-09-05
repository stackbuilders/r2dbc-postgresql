package io.r2dbc.postgresql.codec;

import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.message.Format.FORMAT_TEXT;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.JSON;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.JSONB;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.type.PostgresqlObjectId;
import io.r2dbc.postgresql.util.Assert;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

public class JsonCodec extends AbstractCodec<Object> {
	
	private final ByteBufAllocator byteBufAllocator;
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JsonCodec(ByteBufAllocator byteBufAllocator) {
		super(Object.class);
		this.byteBufAllocator = Assert.requireNonNull(byteBufAllocator, "byteBufAllocator must not be null");
	}

	@Override
	public Parameter encodeNull() {
		return createNull(FORMAT_TEXT, JSON);
	}

	@Override
	boolean doCanDecode(Format format, PostgresqlObjectId type) {
		Assert.requireNonNull(format, "format must not be null");
		Assert.requireNonNull(type, "type must not be null");
		
		return (FORMAT_TEXT == format || FORMAT_BINARY == format) && (JSON == type || JSONB == type);
	}

	@Override
	Object doDecode(ByteBuf byteBuf, Format format, Class<? extends Object> type) {
		Assert.requireNonNull(byteBuf, "byteBuf must not be null");
        Assert.requireNonNull(format, "format must not be null");
        
        if (format == FORMAT_BINARY) {
        	if (ByteBuf.class.isAssignableFrom(type)) {
        		return byteBuf;
        	}
        	
        	if (InputStream.class.isAssignableFrom(type)) {
                return new ByteBufInputStream(byteBuf);
            }
        	
        	byte[] bytes = new byte[byteBuf.readableBytes()];
        	int readIndex = byteBuf.readerIndex();
        	byteBuf.getBytes(readIndex, bytes);
        	
        	return bytes;
        }
        
        if (String.class.isAssignableFrom(type)) {
            return byteBuf.readCharSequence(byteBuf.readableBytes(), UTF_8).toString();
        }
        
        try {
        	Reader reader = new InputStreamReader(new ByteBufInputStream(byteBuf));
        	
			return objectMapper.readValue(reader, type);
		} catch (Throwable t) {
			throw Exceptions.propagate(t);
		}
	}

	@Override
	Parameter doEncode(Object value) {
		Assert.requireNonNull(value, "value must not be null");
		
        try {
        	ByteBuf encoded = byteBufAllocator.buffer();
        	OutputStreamWriter writer = new OutputStreamWriter(new ByteBufOutputStream(encoded));
			
        	objectMapper.writeValue(writer, value);
			writer.close();
			
			return create(FORMAT_TEXT, JSON, Flux.just(encoded));
		} catch (Throwable t) {
			throw Exceptions.propagate(t);
		}
	}

}
