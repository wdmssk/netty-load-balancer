package io.netty.loadbalancer.config;

import java.util.List;

public final class ConfigurationProperties {
    private int localPort;
    private List<HostnamePort> backendServers;

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public List<HostnamePort> getBackendServers() {
        return backendServers;
    }

    public void setBackendServers(List<HostnamePort> backendServers) {
        this.backendServers = backendServers;
    }

    @Override
    public String toString() {
        return "ConfigurationProperties{" +
               "localPort=" + localPort +
               ", backendServers=" + backendServers +
               '}';
    }
}
