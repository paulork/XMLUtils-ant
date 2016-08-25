package br.com.paulork.exceptions;

/**
 * @author Paulo R. Kraemer <paulork10@gmail.com>
 */
public class XMLException extends RuntimeException {

    public XMLException(String message) {
        super(message);
    }

    public XMLException(String message, Throwable cause) {
        super(message, cause);
    }

}
