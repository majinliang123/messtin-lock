package org.messtin.lock.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.common.entity.LockRequest;
import org.messtin.lock.common.entity.LockResponse;
import org.messtin.lock.common.entity.ResponseCode;
import org.messtin.lock.common.entity.Step;
import org.messtin.lock.common.util.StringUtil;
import org.messtin.lock.common.util.UUIDUtil;
import org.messtin.lock.server.container.LockContainer;
import org.messtin.lock.server.container.SessionContainer;
import org.messtin.lock.server.entity.Operator;
import org.messtin.lock.server.entity.Session;

import java.util.List;

/**
 * The handler when request arrived.
 *
 * @author majinliang
 */
public class LockHandler extends SimpleChannelInboundHandler<LockRequest> {
    private static final Logger logger = LogManager.getLogger(LockHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LockRequest request) throws Exception {
        Step step = request.getStep();
        logger.info("Accept {} request.", request);
        switch (step) {
            case Connect:
                connectHandler(ctx, request);
                break;
            case Lock:
                lockHandler(ctx, request);
                break;
            case Unlock:
                unlockHandler(ctx, request);
                break;
            case Close:
                closeHandler(ctx, request);
                break;
            default:
                defaultHandler(ctx, request);
        }
    }

    /**
     * The connection request is：
     * 1. user first connects to server.
     * 2. user re-connect to server.
     * <p>
     * If the request has session id means the client connected to server before,
     * so we will reuse the session id.
     *
     * @param ctx
     * @param request the request from user.
     */
    private void connectHandler(ChannelHandlerContext ctx, LockRequest request) {
        String sessionId = StringUtil.isNotEmpty(request.getSessionId()) ? request.getSessionId() : UUIDUtil.generate();

        logger.info("Read request from sessionId={}.", sessionId);
        Channel channel = ctx.channel();
        Session session = new Session(sessionId, channel);
        boolean isDone = SessionContainer.register(session);

        LockResponse response = new LockResponse();
        if (isDone) {
            response.setResponseCode(ResponseCode.OK);
            response.setSessionId(sessionId);
            response.setStep(Step.Connect);
        } else {
            response.setResponseCode(ResponseCode.Failed);
            response.setSessionId(sessionId);
            response.setStep(Step.Connect);
        }

        logger.info("Write {} to client.", response);
        channel.writeAndFlush(response);
    }

    private void lockHandler(ChannelHandlerContext ctx, LockRequest request) {
        String sessionId = request.getSessionId();
        Channel channel = ctx.channel();

        LockResponse response = new LockResponse();
        if (!SessionContainer.isExist(sessionId)) {
            response.setResponseCode(ResponseCode.Failed);
            response.setSessionId(sessionId);
            response.setStep(Step.Lock);
            response.setResource(request.getResource());
            channel.writeAndFlush(response);
            return;
        }

        Operator operator = new Operator(Step.Lock, channel, request.getSessionId());
        boolean isGet = LockContainer.acquire(request.getResource(), operator);
        if (isGet) {
            response.setResponseCode(ResponseCode.OK);
            response.setSessionId(sessionId);
            response.setStep(Step.Lock);
            response.setResource(request.getResource());
            channel.writeAndFlush(response);
        }
    }

    private void unlockHandler(ChannelHandlerContext ctx, LockRequest request) {
        String sessionId = request.getSessionId();
        String resource = request.getResource();
        Operator operator = LockContainer.release(resource, sessionId);
        if (operator != null) {
            LockResponse response = new LockResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setSessionId(sessionId);
            response.setStep(Step.Lock);
            response.setResource(resource);
            ctx.channel().writeAndFlush(response);
        }
    }

    private void closeHandler(ChannelHandlerContext ctx, LockRequest request) {
        List<Operator> operators = LockContainer.release(request.getSessionId());
        for (Operator operator : operators) {
            if (operator != null) {
                LockResponse response = new LockResponse();
                response.setResponseCode(ResponseCode.OK);
                response.setSessionId(operator.getSessionId());
                response.setStep(Step.Lock);
                response.setResource(null);
                ctx.channel().writeAndFlush(response);
            }
        }
    }

    private void defaultHandler(ChannelHandlerContext ctx, LockRequest request) {
        LockResponse response = new LockResponse();
        response.setResponseCode(ResponseCode.Failed);
        response.setSessionId(request.getSessionId());
        response.setStep(request.getStep());
        response.setResource(request.getResource());
        ctx.channel().writeAndFlush(response);
    }
}
