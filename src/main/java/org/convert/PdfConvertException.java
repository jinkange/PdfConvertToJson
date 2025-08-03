package org.convert;

public class PdfConvertException extends Exception {
    public PdfConvertException(String message) {
        super(message);
    }

    public PdfConvertException(String message, Throwable cause) {
        super(message, cause);
    }
}
