package org.messtin.lock.common.codec;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.messtin.lock.common.util.ProtostuffUtils;

public class LockDeconder extends MessageToByteEncoder {
    private Class<?> genericClass;

    public LockDeconder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            byte[] data = ProtostuffUtils.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
