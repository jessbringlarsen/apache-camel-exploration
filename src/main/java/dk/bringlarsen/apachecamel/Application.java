package dk.bringlarsen.apachecamel;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.List;

public class Application {

    private CamelContext camelContext = new DefaultCamelContext();

    private String directoryToWatch;
    private int sftpServerPort;
    private List<String> responseList;

    public Application(String directoryToWatch, int sftpServerPort, List<String> responseList) {
        this.directoryToWatch = directoryToWatch;
        this.sftpServerPort = sftpServerPort;
        this.responseList = responseList;
    }

    public void startup() throws Exception {
        camelContext.disableJMX();
        camelContext.addRoutes(new FtpRouteBuilder(directoryToWatch, sftpServerPort, responseList));
        camelContext.start();
    }

    public void shutdown() {
        camelContext.stop();
    }
}
