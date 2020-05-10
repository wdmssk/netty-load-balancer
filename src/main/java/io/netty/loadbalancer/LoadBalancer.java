package io.netty.loadbalancer;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.loadbalancer.config.HostnamePort;
import io.netty.loadbalancer.handler.LoadBalancerInitializer;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

import java.net.InetSocketAddress;
import java.util.List;

public final class LoadBalancer {
    private LoadBalancer() {
    }

    public static void bindNow(int localPort, List<HostnamePort> backendServers) throws InterruptedException {

        EventLoopGroup acceptorGroup = new NioEventLoopGroup(1);
        EventLoopGroup clientGroup = new NioEventLoopGroup(backendServers.size());

        Map<EventLoop, Bootstrap> eventLoopToBootstrap = getEventLoopToBootstrap(clientGroup, backendServers);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        try {
            // Configure the bootstrap.
            Channel channel = serverBootstrap.group(acceptorGroup, clientGroup)
                                             .channel(NioServerSocketChannel.class)
                                             .handler(new LoggingHandler(LogLevel.INFO))
                                             .childHandler(new LoadBalancerInitializer(eventLoopToBootstrap))
                                             .childOption(ChannelOption.AUTO_READ, false)
                                             .bind(localPort)
                                             .sync()
                                             .channel();

            channel.closeFuture()
                   .sync();
        } finally {
            acceptorGroup.shutdownGracefully();
            clientGroup.shutdownGracefully();
        }
    }

    static Map<EventLoop, Bootstrap> getEventLoopToBootstrap(EventLoopGroup clientEventLoopGroup,
                                                             List<HostnamePort> backendServers) {
        return Stream.ofAll(clientEventLoopGroup)
                     .zip(backendServers)
                     .toMap(t -> (EventLoop) t._1,
                            t -> backendBootstrap((EventLoop) t._1, t._2.getHostname(), t._2.getPort()));
    }

    static Bootstrap backendBootstrap(EventLoop eventLoop, String hostname, int port) {
        Bootstrap bootstrap = new Bootstrap();

        return bootstrap.group(eventLoop)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.AUTO_READ, false)
                        .remoteAddress(new InetSocketAddress(hostname, port));
    }
}