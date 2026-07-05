package com.imanimen.core;

import com.imanimen.core.config.Configuration;
import com.imanimen.core.config.ConfigurationManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Driver Class for the Jttp Server
 */
public class JttpServer {
    public static void main(String[] args) {
        System.out.println("Starting JttpServer");
        ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/jttp.json");
        Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();

        System.out.println("Using Port: " + conf.getPort());
        System.out.println("Using WebRoot: " + conf.getWebroot());

        // server socket
        try {
            ServerSocket serverSocket = new ServerSocket(conf.getPort());
            Socket socket = serverSocket.accept();

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            String html = "<html><head><title>Jttp Server</title><body><h1>This page was server by JTTP!</h1></body></head></html>";
            final String CRLF = "\r\n"; // 13, 10 ASCII
            String response =
                    "HTTP/1.1 200 OK" + CRLF + // status line: HTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
                    "Content-Length: " + html.getBytes().length + CRLF +
                    CRLF +
                    html +
                    CRLF + CRLF;

            outputStream.write(response.getBytes());
            inputStream.close();
            outputStream.close();
            socket.close();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
