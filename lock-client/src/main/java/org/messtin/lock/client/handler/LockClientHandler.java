package org.messtin.lock.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.messtin.lock.common.entity.LockResponse;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class LockClientHandler extends SimpleChannelInboundHandler<LockResponse> {
    private Map<String, CountDownLatch> lockMap;

    public LockClientHandler(Map<String, CountDownLatch> lockMap) {
        this.lockMap = lockMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LockResponse msg) throws Exception {
        String resource = msg.getResource();
        lockMap.get(resource).countDown();
    }
}
