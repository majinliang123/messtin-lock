package org.messtin.lock.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.common.entity.LockResponse;
import org.messtin.lock.common.entity.Step;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LockClientHandler extends SimpleChannelInboundHandler<LockResponse> {
    private static final Logger logger = LogManager.getLogger(LockClientHandler.class);

    private Map<String, CountDownLatch> lockMap;

    public LockClientHandler(Map<String, CountDownLatch> lockMap) {
        this.lockMap = lockMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LockResponse msg) {
        Step step = msg.getStep();
        if (step == Step.Connect) {
            return;
        }
        logger.info(msg);
        logger.info(lockMap);
        String resource = msg.getResource();
        lockMap.get(resource).countDown();
    }
}
