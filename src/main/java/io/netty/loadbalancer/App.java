package io.netty.loadbalancer;

import io.netty.loadbalancer.config.ConfigurationProperties;
import io.netty.loadbalancer.config.HostnamePort;
import io.vavr.control.Try;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "nettyLoadBalancer-1.0-SNAPSHOT-all.jar", mixinStandardHelpOptions = true, version = "1.0-SNAPSHOT", description = "Starts the netty load balancer.")
public final class App implements Callable<Integer> {
    private static final Logger logger = LogManager.getLogger(App.class);

    @Option(names = { "-c", "--config" }, required = true, description = "Path of the configuration file.")
    private String configFilePath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws InterruptedException {

        Try<ConfigurationProperties> configurationProperties = getConfigurationProperties(configFilePath);
        if (configurationProperties.isFailure()) {
            return 1;
        }
        ConfigurationProperties properties = configurationProperties.get();

        // TODO: Validate properties via bean validation
        final int localPort = properties.getLocalPort();
        final List<HostnamePort> backendServers = properties.getBackendServers();

        logger.info("Starting proxy server on local port {}.{}Backend servers:", localPort, System.lineSeparator());
        for (HostnamePort backendServer : backendServers) {
            logger.info("{}:{}", backendServer.getHostname(), backendServer.getPort());
        }

        LoadBalancer.bindNow(localPort, backendServers);
        return 0;
    }

    // TODO: Create shutdown method.

    private static Try<ConfigurationProperties> getConfigurationProperties(String configFilePath) {
        return Try.of(() -> Paths.get(configFilePath))
                  .flatMap(filePath -> Try.withResources(() -> Files.newInputStream(filePath))
                                          .of(inStream -> new Yaml().loadAs(inStream, ConfigurationProperties.class)))
                  .onFailure(ex -> System.err
                          .println("Error while loading the configuration file:" + System.lineSeparator() + ex));
    }
}