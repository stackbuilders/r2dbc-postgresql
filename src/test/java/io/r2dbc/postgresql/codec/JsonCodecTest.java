package io.r2dbc.postgresql.codec;

import static io.r2dbc.postgresql.client.Parameter.NULL_VALUE;
import static io.r2dbc.postgresql.client.ParameterAssert.assertThat;
import static io.r2dbc.postgresql.message.Format.FORMAT_BINARY;
import static io.r2dbc.postgresql.message.Format.FORMAT_TEXT;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.JSON;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.JSONB;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.MONEY;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.VARCHAR;
import static io.r2dbc.postgresql.util.TestByteBufAllocator.TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.util.ByteBufUtils;
import io.r2dbc.postgresql.util.JsonPojo;

public class JsonCodecTest {
	
	@Test
	void constructorNoByteBufAllocator() {
	    assertThatIllegalArgumentException().isThrownBy(() -> new JsonCodec(null))
		    .withMessage("byteBufAllocator must not be null");
	}
	
	@Test
	void decode() throws IOException {
	    final String json = "{\"name\": \"John Doe\"}";
	    final JsonCodec jsonCodec = new JsonCodec(TEST);
	    final InputStream decodedStream = (InputStream) jsonCodec.decode(ByteBufUtils.encode(TEST, json), FORMAT_BINARY, InputStream.class);
	    Map<String, Object> map = new HashMap<>();
        map.put("name", "John Doe");
        JsonPojo jsonPojo = new JsonPojo();
        jsonPojo.setName("John Doe");
        
	    assertThat(decodedStream.readAllBytes()).isEqualTo(json.getBytes());
	    assertThat(jsonCodec.decode(ByteBufUtils.encode(TEST, json), FORMAT_BINARY, ByteBuf.class)).isEqualTo(ByteBufUtils.encode(TEST, json));
	    assertThat(jsonCodec.decode(ByteBufUtils.encode(TEST, json), FORMAT_BINARY, Byte.class)).isEqualTo(json.getBytes());
	    assertThat(jsonCodec.decode(ByteBufUtils.encode(TEST, json), FORMAT_TEXT, String.class)).isEqualTo(json);	    
	    assertThat(jsonCodec.decode(ByteBufUtils.encode(TEST, json), FORMAT_TEXT, Map.class)).isEqualTo(Map.of("name", "John Doe"));
	    assertThat(jsonCodec.decode(ByteBufUtils.encode(TEST, json), FORMAT_TEXT, JsonPojo.class)).isEqualToComparingFieldByField(jsonPojo);
	}
	
	@Test
    void decodeNoByteBuf() {
        assertThat(new JsonCodec(TEST).decode(null, FORMAT_TEXT, Boolean.class)).isNull();
        assertThat(new JsonCodec(TEST).decode(null, FORMAT_BINARY, Boolean.class)).isNull();
    }
	
	@Test
	void doCanDecode() {
	    JsonCodec jsonCodec = new JsonCodec(TEST);
	    
	    assertThat(jsonCodec.doCanDecode(FORMAT_TEXT, JSON)).isTrue();
	    assertThat(jsonCodec.doCanDecode(FORMAT_BINARY, JSON)).isTrue();
	    assertThat(jsonCodec.doCanDecode(FORMAT_TEXT, JSONB)).isTrue();
        assertThat(jsonCodec.doCanDecode(FORMAT_BINARY, JSONB)).isTrue();
        assertThat(jsonCodec.doCanDecode(FORMAT_TEXT, MONEY)).isFalse();
        assertThat(jsonCodec.doCanDecode(FORMAT_BINARY, MONEY)).isFalse();
	}
	
	@Test
    void doCanDecodeNoFormat() {
        assertThatIllegalArgumentException().isThrownBy(() -> new JsonCodec(TEST).doCanDecode(null, VARCHAR))
            .withMessage("format must not be null");
    }
	
	@Test
    void doCanDecodeNoType() {
        assertThatIllegalArgumentException().isThrownBy(() -> new JsonCodec(TEST).doCanDecode(FORMAT_TEXT, null))
            .withMessage("type must not be null");
	}
	
	@Test
	void doEncode() {
	    final String completeJson = "{"
	            + "\"name\":\"John Doe\","
	            + "\"age\":null,"
	            + "\"birthdate\":null,"
	            + "\"isAdmin\":null,"
	            + "\"profiles\":null,"
	            + "\"attrs\":null,"
	            + "\"nullable\":null"
	        + "}";
	    final String json = "{\"name\":\"John Doe\"}";
	    final JsonCodec jsonCodec = new JsonCodec(TEST);
	    Map<String, Object> map = new HashMap<>();
        map.put("name", "John Doe");
        JsonPojo jsonPojo = new JsonPojo();
        jsonPojo.setName("John Doe");
        
        assertThat(jsonCodec.doEncode(jsonPojo))
            .hasFormat(FORMAT_TEXT)
            .hasType(JSON.getObjectId())
            .hasValue(ByteBufUtils.encode(TEST, completeJson));
        
        assertThat(jsonCodec.doEncode(map))
            .hasFormat(FORMAT_TEXT)
            .hasType(JSON.getObjectId())
            .hasValue(ByteBufUtils.encode(TEST, json));
	}
	
	 @Test
	    void doEncodeNoValue() {
	        assertThatIllegalArgumentException().isThrownBy(() -> new JsonCodec(TEST).doEncode(null))
	            .withMessage("value must not be null");
	    }

	    @Test
	    void encodeNull() {
	        assertThat(new JsonCodec(TEST).encodeNull())
	            .isEqualTo(new Parameter(FORMAT_TEXT, JSON.getObjectId(), NULL_VALUE));
	    }

}
