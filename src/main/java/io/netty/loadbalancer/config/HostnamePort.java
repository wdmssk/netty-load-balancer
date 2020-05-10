package io.netty.loadbalancer.config;

public final class HostnamePort {
    private String hostname;
    private int port;

    public static HostnamePort of(String hostname, int port) {
        HostnamePort hostnamePort = new HostnamePort();
        hostnamePort.hostname = hostname;
        hostnamePort.port = port;
        return hostnamePort;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "HostnamePort{" +
               "hostname='" + hostname + '\'' +
               ", port=" + port +
               '}';
    }
}
