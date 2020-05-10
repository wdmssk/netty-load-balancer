package io.netty.loadbalancer.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public class NettyUtils {

    /**
     * Closes the specified channel after all queued write requests are flushed.
     *
     * @param ch {@link Channel} to be closed
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
