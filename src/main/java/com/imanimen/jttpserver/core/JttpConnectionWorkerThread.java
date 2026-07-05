package com.imanimen.jttpserver.core;

import com.imanimen.jttpserver.JttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class JttpConnectionWorkerThread extends Thread{
    private final static Logger LOGGER = LoggerFactory.getLogger(JttpServer.class);
    private final Socket socket;

    public JttpConnectionWorkerThread(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
             inputStream = socket.getInputStream();
             outputStream = socket.getOutputStream();

            String response = htmlContent();

            outputStream.write(response.getBytes());


            LOGGER.info("Connection processing finished");
        } catch (IOException e) {
            LOGGER.error("Poblem with communication " + e);
            throw new RuntimeException(e);
        } finally{
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {}
            }
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException ignored) {}
            }
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
    private static String htmlContent() {
        String html = "<html><head><title>Jttp Server</title><body><h1>This page was server by JTTP!</h1></body></head></html>";
        final String CRLF = "\r\n"; // 13, 10 ASCII
        return "HTTP/1.1 200 OK" + CRLF + // status line: HTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
                "Content-Length: " + html.getBytes().length + CRLF +
                CRLF +
                html +
                CRLF + CRLF;
    }
}
