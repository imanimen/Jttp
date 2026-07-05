package com.imanimen.jttpserver.config;

public class JttpConfigurationException extends RuntimeException {
    public JttpConfigurationException() {
    }

    public JttpConfigurationException(String message) {
        super(message);
    }

    public JttpConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JttpConfigurationException(Throwable cause) {
        super(cause);
    }

}
