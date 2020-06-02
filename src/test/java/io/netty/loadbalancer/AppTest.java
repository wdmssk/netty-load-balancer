package io.netty.loadbalancer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Some tests were deactivated because System Rules can't be used with JUnit5
 */
public class AppTest {
    private static final Logger logger = LogManager.getLogger(AppTest.class);

    private static final String CONFIG_FILE = "config.yml";

    // private static final String HELP_MSG =
    //"usage: nettyLoadBalancer-1.0-SNAPSHOT-all.jar -c <arg> [-h]\n -c,--config <arg>\n -h,--help\n";
    // private static final String ERROR_MISSING_C =
    // "Missing required option: c\n";

    @Test
    public void test_main_help() throws InterruptedException {
        logger.info("test_main_help - start");

        String[] args = { "-h" };
        App.main(args);
    }

    public void test_main_commandLineParseErr() throws InterruptedException {
        logger.info("test_main_commandLineParseErr - start");

        String[] args = { "" };
        App.main(args);
    }

    public void test_main_configFilePathErr() throws InterruptedException {
        logger.info("test_main_configFilePathErr - start");

        String[] args = { "-c 11@@AABB" };
        App.main(args);
    }

    public void test_main_configFileFormatErr() throws InterruptedException, URISyntaxException {
        logger.info("test_main_configFileFormatErr - start");

        String[] args = { "-c", Paths.get(ClassLoader.getSystemResource("hostnamePort.yml").toURI()).toString() };
        logger.info("args: {}", Arrays.toString(args));
        App.main(args);
    }

    public void test_main() throws InterruptedException, URISyntaxException {
        logger.info("test_main - start");

        String[] args = { "-c", Paths.get(ClassLoader.getSystemResource(CONFIG_FILE).toURI()).toString() };
        logger.info("args: {}", Arrays.toString(args));
        App.main(args);

        // server shutdown
    }
}
