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

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("channelInactive:{}", ctx.channel());
        Channel channel = ctx.channel();
        Session session = SessionContainer.get(channel);
        closeSession(session.getSessionId());
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
        logger.info("Handle connect request with sessionId={}.", sessionId);

        Channel channel = ctx.channel();
        Session session = new Session(sessionId, channel);
        boolean isDone = SessionContainer.register(session);

        LockResponse response;
        if (isDone) {
            response = buildResponse(request, ResponseCode.OK);
        } else {
            response = buildResponse(request, ResponseCode.Failed);
        }
        response.setSessionId(sessionId);

        logger.info("Send {} to client.", response);
        channel.writeAndFlush(response);
    }

    /**
     * When handle lock request, we will first check if the client connected to server.
     * If not connected before, will send {@link ResponseCode} as Failed.
     * <p>
     * If it connected before, will try to acquire lock.
     * If acquire lock failed, will not send client any response,
     * so client will wait for the response until server send response when get lock resource.
     *
     * @param ctx
     * @param request
     */
    private void lockHandler(ChannelHandlerContext ctx, LockRequest request) {
        String sessionId = request.getSessionId();
        Channel channel = ctx.channel();
        logger.info("Handle lock request with sessionId={}.", sessionId);

        if (!SessionContainer.isExist(sessionId)) {
            LockResponse response = buildResponse(request, ResponseCode.Failed);
            channel.writeAndFlush(response);
            return;
        }

        Operator operator = new Operator(Step.Lock, channel, request.getSessionId(), request.getResource());
        boolean isGet = LockContainer.acquire(request.getResource(), operator);
        if (isGet) {
            LockResponse response = buildResponse(request, ResponseCode.OK);
            channel.writeAndFlush(response);
        }
    }

    /**
     * After we unlock the resource, we will check if there are any other client wait for the resource.
     * If existed, we need pick the head of them to send response which means get resource lock successfully.
     *
     * @param ctx
     * @param request
     */
    private void unlockHandler(ChannelHandlerContext ctx, LockRequest request) {
        String sessionId = request.getSessionId();
        logger.info("Handle unlock request with sessionId={}.", sessionId);

        String resource = request.getResource();
        Operator operator = LockContainer.release(resource, sessionId);
        if (operator != null) {
            LockResponse response = new LockResponse();
            response.setResponseCode(ResponseCode.OK);
            response.setSessionId(operator.getSessionId());
            response.setStep(Step.Lock);
            response.setResource(operator.getResource());
            operator.getChannel().writeAndFlush(response);
        }
    }

    /**
     * When client close connection with server.
     * 1. remove session at {@link SessionContainer}
     * 2. remove all {@link Operator}.
     * 3. trigger the {@link Operator} which wait for the resource.
     *
     * @param ctx
     * @param request
     */
    private void closeHandler(ChannelHandlerContext ctx, LockRequest request) {
        closeSession(request.getSessionId());
    }

    private void defaultHandler(ChannelHandlerContext ctx, LockRequest request) {
        LockResponse response = buildResponse(request, ResponseCode.Failed);
        ctx.channel().writeAndFlush(response);
    }

    private LockResponse buildResponse(LockRequest request, ResponseCode resCode) {
        LockResponse response = new LockResponse();
        response.setResponseCode(resCode);
        response.setSessionId(request.getSessionId());
        response.setStep(request.getStep());
        response.setResource(request.getResource());
        return response;
    }

    private void closeSession(String sessionId) {
        SessionContainer.deregister(sessionId);
        List<Operator> operators = LockContainer.release(sessionId);
        for (Operator operator : operators) {
            if (operator != null) {
                LockResponse response = new LockResponse();
                response.setResponseCode(ResponseCode.OK);
                response.setSessionId(operator.getSessionId());
                response.setStep(operator.getStep());
                response.setResource(operator.getResource());
                operator.getChannel().writeAndFlush(response);
            }
        }
    }
}
