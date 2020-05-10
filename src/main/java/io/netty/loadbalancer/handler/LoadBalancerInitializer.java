package io.netty.loadbalancer.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.vavr.collection.Map;

public class LoadBalancerInitializer extends ChannelInitializer<SocketChannel> {

    private final Map<EventLoop, Bootstrap> eventLoopToBootstrap;

    public LoadBalancerInitializer(Map<EventLoop, Bootstrap> eventLoopToBootstrap) {
        this.eventLoopToBootstrap = eventLoopToBootstrap;
    }

    @Override
    public void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(
                new LoggingHandler(LogLevel.INFO),
                new FrontendHandler(eventLoopToBootstrap));
    }
}
