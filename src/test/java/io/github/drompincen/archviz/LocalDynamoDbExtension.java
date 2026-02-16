package io.github.drompincen.archviz;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.ServerSocket;

public class LocalDynamoDbExtension implements BeforeAllCallback, AfterAllCallback {

    private DynamoDBProxyServer server;
    private int port;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        port = findFreePort();
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", String.valueOf(port)});
        server.start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public int getPort() {
        return port;
    }

    public String getEndpoint() {
        return "http://localhost:" + port;
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
