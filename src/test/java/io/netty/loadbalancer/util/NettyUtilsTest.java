package io.netty.loadbalancer.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class NettyUtilsTest {
    private static final Logger logger = LogManager.getLogger(NettyUtilsTest.class);

    @Test
    public void test_closeOnFlush() {
        logger.info("test_closeOnFlush - start");

        Try<ServerSocket> socket = Try.of(() -> new ServerSocket()).flatMap(
                s -> Try.of(() -> {
                    s.bind(new InetSocketAddress(0));
                    return s;
                })
        );
        assertThat(socket.isSuccess(), equalTo(true));


        NioEventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                 .channel(NioSocketChannel.class)
                 .handler(new ChannelInboundHandlerAdapter());

        ChannelFuture channelFuture = bootstrap.connect(socket.get().getLocalSocketAddress());
        Channel channel = channelFuture.syncUninterruptibly()
                                       .channel();

        assertThat(channel.isActive(), equalTo(true));
        NettyUtils.closeOnFlush(channel);

        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.syncUninterruptibly();
        assertThat(channel.isOpen(), equalTo(false));
    }
}