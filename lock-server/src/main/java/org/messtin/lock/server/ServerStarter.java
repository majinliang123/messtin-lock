package org.messtin.lock.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.messtin.lock.common.codec.LockEncoder;
import org.messtin.lock.common.codec.LockDecoder;
import org.messtin.lock.common.entity.LockRequest;
import org.messtin.lock.common.entity.LockResponse;
import org.messtin.lock.server.handler.LockHandler;

/**
 * Main function of server.
 *
 * @author majinliang
 */
public final class ServerStarter {
    private static final Logger logger = LogManager.getLogger(ServerStarter.class);

    private static final int PORT = 4043;

    public static void main(String[] args) throws InterruptedException {
        logger.info("Start initialise server.");

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LockDecoder(LockRequest.class))
                                    .addLast(new LockEncoder(LockResponse.class))
                                    .addLast(new LockHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(PORT).sync();
            logger.info("Server listen at: {}", PORT);

            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("Sever is shutdown.");
        }

    }
}
