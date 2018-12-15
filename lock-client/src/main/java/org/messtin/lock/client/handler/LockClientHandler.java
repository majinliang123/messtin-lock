package org.messtin.lock.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.client.connector.Connector;
import org.messtin.lock.common.entity.LockResponse;
import org.messtin.lock.common.entity.ResponseCode;
import org.messtin.lock.common.entity.Step;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * Use to process server response.
 *
 * @author majinliang
 */
public class LockClientHandler extends SimpleChannelInboundHandler<LockResponse> {
    private static final Logger logger = LogManager.getLogger(LockClientHandler.class);

    private Map<String, Queue<CountDownLatch>> lockMap;

    public LockClientHandler(Map<String, Queue<CountDownLatch>> lockMap) {
        this.lockMap = lockMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LockResponse response) {
        logger.info("Accept response={}.", response);

        if (response.getResponseCode() == ResponseCode.Failed) {
            throw new RuntimeException("Failed get response=" + response);
        }

        Step step = response.getStep();
        switch (step) {
            case Connect:
                Connector.setSessionId(response.getSessionId());
                break;
            case Lock:
                String resource = response.getResource();
                logger.info("==================================");
                logger.info(lockMap);
                lockMap.get(resource).poll().countDown();
                break;
            case Unlock:
            case Close:
                logger.info("Success {} resource={}.", step, response.getResource());
                break;
            default:
                throw new RuntimeException("Response step is illegal. response=" + response);
        }
    }
}
