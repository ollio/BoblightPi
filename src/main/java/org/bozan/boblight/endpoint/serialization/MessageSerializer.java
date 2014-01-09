package org.bozan.boblight.endpoint.serialization;

import org.springframework.integration.ip.tcp.serializer.ByteArraySingleTerminatorSerializer;

import java.io.IOException;
import java.io.OutputStream;

public class MessageSerializer extends ByteArraySingleTerminatorSerializer {

    public MessageSerializer() {
        super((byte) 0x0a);
    }

    @Override
    public void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
        if(bytes != null) {
            super.serialize(bytes, outputStream);
        }
    }
}
