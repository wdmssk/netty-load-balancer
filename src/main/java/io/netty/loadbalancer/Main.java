package io.netty.loadbalancer;

import io.netty.loadbalancer.config.ConfigurationProperties;
import io.netty.loadbalancer.config.HostnamePort;
import io.vavr.control.Try;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static final String H_OPTN = "h";
    public static final String C_OPTN = "c";

    public static final String JAR_FILE_NAME = "nettyLoadBalancer-1.0-SNAPSHOT-all.jar";

    public static void main(String[] args) throws InterruptedException {

        Options optns = getOptions();
        if (hasHelpOption(optns.getOption(H_OPTN), args)) {
            printHelp(optns);
            return;
        }

        Try<ConfigurationProperties> configurationProperties =
                getConfigurationProperties(getConfigFilePath(getCommandLine(optns, args)));
        configurationProperties.onFailure(ex -> System.exit(1));

        ConfigurationProperties properties = configurationProperties.get();
        // TODO: Validate properties via bean validation

        final int localPort = properties.getLocalPort();
        final List<HostnamePort> backendServers = properties.getBackendServers();

        logger.info("Starting proxy server on local port {}.{}Backend servers:", localPort, System.lineSeparator());
        for (HostnamePort backendServer : backendServers) {
            logger.info("{}:{}", backendServer.getHostname(), backendServer.getPort());
        }

        LoadBalancer.bindNow(localPort, backendServers);
    }

    // TODO: Create shutdown method.

    private static Options getOptions() {
        Options ret = new Options();
        ret.addOption(Option.builder(H_OPTN).longOpt("help").required(false).hasArg(false).build());
        ret.addOption(Option.builder(C_OPTN).longOpt("config").required(true).hasArg(true).build());
        return ret;
    }

    private static Boolean hasHelpOption(final Option helpOption, final String[] args) {
        Options options = new Options();
        options.addOption(helpOption);

        // failure not expected with the helpOption
        return getCommandLine(options, args)
                .map(commandLine -> commandLine.hasOption(helpOption.getOpt()))
                .get();
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(JAR_FILE_NAME, options, true);
    }

    private static Try<Path> getConfigFilePath(Try<CommandLine> commandLine) {
        return commandLine
                .flatMap(cmndLine -> Try.of(() -> Paths.get(cmndLine.getParsedOptionValue(C_OPTN).toString()))
                                        .onFailure(ex -> System.err.println(ex.getMessage())) //  failure not expected
                );
    }

    private static Try<ConfigurationProperties> getConfigurationProperties(Try<Path> configFilePath) {
        return configFilePath
                .flatMap(filePath -> Try.withResources(() -> Files.newInputStream(filePath))
                                        .of(inStream -> new Yaml().loadAs(inStream, ConfigurationProperties.class))
                                        .onFailure(ex -> System.err.println(
                                                "Error while loading the configuration file:" + System.lineSeparator() +
                                                ex))
                );
    }

    private static Try<CommandLine> getCommandLine(Options optns, String[] args) {
        return Try.of(() -> new DefaultParser().parse(optns, args, true))
                  .onFailure(ex -> System.err.println(ex.getMessage()));
    }
}