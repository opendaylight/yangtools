package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

public class InferenceException extends SourceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InferenceException(String message, StatementSourceReference source, Throwable cause) {
        super(message, source, cause);
    }

    public InferenceException(String message, StatementSourceReference source) {
        super(message, source);
    }




}
