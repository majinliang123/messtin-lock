package org.messtin.lock.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.common.util.ProtostuffUtils;

import java.util.List;

/**
 * The decoder. Use to convert byte stream to message.
 *
 * @author majinliang
 */
public class LockDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LogManager.getLogger(LockDecoder.class);

    private Class<?> genericClass;

    public LockDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int msgLen = in.readInt();
        logger.info("Read {} bytes and convert into {}.", msgLen, genericClass);
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
