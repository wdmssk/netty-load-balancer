package io.netty.loadbalancer;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.BootstrapConfig;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.loadbalancer.config.HostnamePort;
import io.netty.util.concurrent.EventExecutor;
import io.vavr.collection.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class LoadBalancerTest {
    private static final Logger logger = LogManager.getLogger(LoadBalancerTest.class);

    public static final String HOSTNAME = "localhost";
    public static final int PORT_0 = 0;
    public static final int PORT_1 = 1;

    @Test
    public void test_backendBootstrap() {
        final EventLoopGroup group = new NioEventLoopGroup(1);
        final EventLoop eventLoop = group.next();

        Bootstrap bootstrap = LoadBalancer.backendBootstrap(eventLoop, HOSTNAME, PORT_0);
        bootstrap.handler(new ChannelInboundHandlerAdapter());

        BootstrapConfig bootstrapConfig = bootstrap.config();

        InetSocketAddress inetSocketAddress = (InetSocketAddress) bootstrapConfig.remoteAddress();
        assertThat(inetSocketAddress.getHostName(), equalTo(HOSTNAME));
        assertThat(inetSocketAddress.getPort(), equalTo(PORT_0));

        assertThat(bootstrapConfig.group(), equalTo(eventLoop));
    }


    @Test
    public void test_getEventLoopToBootstrap() {
        final EventLoopGroup clientEventLoopGroup = new NioEventLoopGroup(2);

        List<HostnamePort> backendServers =
                List.of(HostnamePort.of(HOSTNAME, PORT_0), HostnamePort.of(HOSTNAME, PORT_1));

        Map<EventLoop, Bootstrap> eventLoopToBootstrap =
                LoadBalancer.getEventLoopToBootstrap(clientEventLoopGroup, backendServers);

        Iterator<EventExecutor> eventLoopIt = clientEventLoopGroup.iterator();
        Iterator<HostnamePort> backendServerIt = backendServers.iterator();

        while (eventLoopIt.hasNext()) {
            EventLoop eventLoop = (EventLoop) eventLoopIt.next();
            HostnamePort hostnamePort = backendServerIt.next();

            Bootstrap bootstrap = eventLoopToBootstrap.get(eventLoop).get();
            BootstrapConfig bootstrapConfig = bootstrap.config();

            assertThat(bootstrapConfig.group(), equalTo(eventLoop));

            InetSocketAddress inetSocketAddress = (InetSocketAddress) bootstrapConfig.remoteAddress();
            assertThat(inetSocketAddress.getHostName(), equalTo(hostnamePort.getHostname()));
            assertThat(inetSocketAddress.getPort(), equalTo(hostnamePort.getPort()));
        }
    }
}
