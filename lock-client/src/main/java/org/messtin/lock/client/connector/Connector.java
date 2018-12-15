package org.messtin.lock.client.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.messtin.lock.client.handler.LockClientHandler;
import org.messtin.lock.common.codec.LockDeconder;
import org.messtin.lock.common.codec.LockEncoder;
import org.messtin.lock.common.entity.LockRequest;
import org.messtin.lock.common.entity.LockResponse;
import org.messtin.lock.common.entity.Step;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Connector {
    private String address;
    private int port;
    private Map<String, CountDownLatch> lockMap;
    private Channel channel;

    public Connector(String address, int port, Map<String, CountDownLatch> lockMap) {
        this.address = address;
        this.port = port;
        this.lockMap = lockMap;
    }

    public void init() {
        NioEventLoopGroup workers = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap()
                    .group(workers)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LockDeconder(LockResponse.class))
                                    .addLast(new LockEncoder(LockRequest.class))
                                    .addLast(new LockClientHandler(lockMap));
                        }
                    });
            channel = b.connect(address, port).sync().channel();
        } catch (InterruptedException e) {

        } finally {
            workers.shutdownGracefully();
        }
    }

    public void sendLockRequest(String resource) {
        LockRequest request = new LockRequest();
        request.setResource(resource);
        request.setStep(Step.Lock);
        request.setSessionId("1");
        channel.writeAndFlush(request);
    }

    public void sendReleaseRequest(String resource) {
        LockRequest request = new LockRequest();
        request.setResource(resource);
        request.setStep(Step.Unlock);
        request.setSessionId("1");
        channel.writeAndFlush(request);
    }
}
