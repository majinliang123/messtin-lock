package org.messtin.lock.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.common.util.ProtostuffUtils;

/**
 * The encoder. Use to convert message to byte stream.
 *
 * @author majinliang
 */
public class LockEncoder extends MessageToByteEncoder {
    private static final Logger logger = LogManager.getLogger(LockEncoder.class);

    private Class<?> genericClass;

    public LockEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        logger.info("Convert {} to byte stream.", msg);
        if (genericClass.isInstance(msg)) {

            byte[] data = ProtostuffUtils.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
