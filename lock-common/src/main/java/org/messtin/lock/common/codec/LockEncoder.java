package org.messtin.lock.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.messtin.lock.common.util.ProtostuffUtils;

import java.util.List;

public class LockEncoder extends ByteToMessageDecoder {
    private Class<?> genericClass;

    public LockEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int msgLen = in.readInt();
        if (in.readableBytes() < msgLen) {
            in.resetReaderIndex();
            return;
        }
        byte[] msg = new byte[msgLen];
        in.readBytes(msg);
        Object object = ProtostuffUtils.deserialize(msg, genericClass);
        out.add(object);
    }
}
