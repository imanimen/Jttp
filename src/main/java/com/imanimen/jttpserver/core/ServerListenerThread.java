package com.imanimen.jttpserver.core;

import com.imanimen.jttpserver.JttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class ServerListenerThread extends Thread {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerListenerThread.class);

    private int port;
    private String webroot;
    private ServerSocket serverSocket;

    public ServerListenerThread(int port, String webroot) throws IOException {
        this.port = port;
        this.webroot = webroot;
        this.serverSocket = new ServerSocket(this.port);
    }

    @Override
    public void run() {

        try {
            while (serverSocket.isBound() && ! serverSocket.isClosed()) {

                Socket socket = serverSocket.accept();
                LOGGER.info("accepted connection from {}", socket.getInetAddress());

                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                String response = htmlContent();

                outputStream.write(response.getBytes());
                inputStream.close();
                outputStream.close();
                socket.close();

                // issue: connections got queue below is the demonstration:
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            // todo handle later
            // serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String htmlContent() {
        String html = "<html><head><title>Jttp Server</title><body><h1>This page was server by JTTP!</h1></body></head></html>";
        final String CRLF = "\r\n"; // 13, 10 ASCII
        String response =
                "HTTP/1.1 200 OK" + CRLF + // status line: HTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
                        "Content-Length: " + html.getBytes().length + CRLF +
                        CRLF +
                        html +
                        CRLF + CRLF;
        return response;
    }
}
