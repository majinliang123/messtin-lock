package org.messtin.lock.client.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.client.handler.LockClientHandler;
import org.messtin.lock.common.codec.LockEncoder;
import org.messtin.lock.common.codec.LockDecoder;
import org.messtin.lock.common.entity.LockRequest;
import org.messtin.lock.common.entity.LockResponse;
import org.messtin.lock.common.entity.Step;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

/**
 * Use to connect with server.
 *
 * @author majinliang
 */
public class Connector {
    private static final Logger logger = LogManager.getLogger(Connector.class);

    private String address;
    private int port;
    private Map<String, Queue<CountDownLatch>> lockMap;
    private Channel channel;

    private static String SESSION_ID;
    private static CountDownLatch sessionInitLatch = new CountDownLatch(1);

    public Connector(String address, int port, Map<String, Queue<CountDownLatch>> lockMap) {
        this.address = address;
        this.port = port;
        this.lockMap = lockMap;
    }

    public void init() throws InterruptedException {
        logger.info("Start initialise connector.");

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
                                    .addLast(new LockDecoder(LockResponse.class))
                                    .addLast(new LockEncoder(LockRequest.class))
                                    .addLast(new LockClientHandler(lockMap));
                        }
                    });

            channel = b.connect(address, port).sync().channel();
            logger.info("Complete initialise connector use netty.");

            sendConnectRequest();
        } finally {
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> workers.shutdownGracefully(), "Messtin-Lock-JVM-shutdown-hook"));
        }
    }

    public void sendConnectRequest() throws InterruptedException {
        logger.info("Connector sends connect request to server.");

        LockRequest request = new LockRequest();
        request.setStep(Step.Connect);
        request.setSessionId(SESSION_ID);

        channel.writeAndFlush(request);
        sessionInitLatch.await();
    }

    public void sendLockRequest(String resource) {
        logger.info("Connector sends lock request to server.");

        LockRequest request = new LockRequest();
        request.setResource(resource);
        request.setStep(Step.Lock);
        request.setSessionId(SESSION_ID);

        channel.writeAndFlush(request);
    }

    public void sendReleaseRequest(String resource) {
        logger.info("Connector sends release request to server.");

        LockRequest request = new LockRequest();
        request.setResource(resource);
        request.setStep(Step.Unlock);
        request.setSessionId(SESSION_ID);

        channel.writeAndFlush(request);
    }

    public static void setSessionId(String sessionId) {
        logger.info("Set client session id as: {}.", sessionId);
        Connector.SESSION_ID = sessionId;
        sessionInitLatch.countDown();
    }
}
