package io.netty.loadbalancer.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.loadbalancer.util.NettyUtils;
import io.vavr.collection.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FrontendHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(FrontendHandler.class);

    private final Map<EventLoop, Bootstrap> eventLoopToBootstrap;

    // Boostrap with the inboundChannel eventLoop is retrieved from the eventLoopToBootstrap map.
    // Since the outbound and inboudChannel use the same EventLoop (and therefore Thread), this does not need to be volatile.
    private Channel outboundChannel;

    public FrontendHandler(Map<EventLoop, Bootstrap> eventLoopToBootstrap) {
        this.eventLoopToBootstrap = eventLoopToBootstrap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();
        Bootstrap bootstrap = eventLoopToBootstrap.get(inboundChannel.eventLoop()).get().clone();
        bootstrap.handler(new BackendHandler(inboundChannel));

        ChannelFuture f = bootstrap.connect();
        outboundChannel = f.channel();

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    // connection complete start to read first data
                    inboundChannel.read();
                } else {
                    // Close the connection if the connection attempt has failed.
                    inboundChannel.close();
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            NettyUtils.closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        NettyUtils.closeOnFlush(ctx.channel());
    }
}
