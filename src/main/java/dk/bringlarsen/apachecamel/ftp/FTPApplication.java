package dk.bringlarsen.apachecamel.ftp;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Application that watches a SFTP directory and notify subscribers when a file is downloaded.
 */
public class FTPApplication {

    private final Logger logger = LoggerFactory.getLogger(FTPApplication.class);
    private final CamelContext camelContext = new DefaultCamelContext();
    private final List<Function<String, Void>> subscribers;
    private final String directoryToWatch;
    private final int sftpServerPort;

    public FTPApplication(String directoryToWatch, int sftpServerPort) {
        this.directoryToWatch = directoryToWatch;
        this.sftpServerPort = sftpServerPort;
        this.subscribers = new ArrayList<>();
    }

    public void startup() {
        try {
            camelContext.disableJMX();
            camelContext.addRoutes(new FtpRouteBuilder(directoryToWatch, sftpServerPort, subscribers));
            camelContext.start();
        } catch(Exception e) {
            logger.error("Failure during startup!", e);
            System.exit(1);
        }
    }

    public void addSubscriber(Function<String, Void> subscriber) {
        subscribers.add(subscriber);
    }

    public void shutdown() {
        camelContext.stop();
    }
}
