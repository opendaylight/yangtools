package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.base.Preconditions;

public class SourceException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final StatementSourceReference sourceRef;

    public SourceException(String message,StatementSourceReference source) {
        super(Preconditions.checkNotNull(message));
        sourceRef = Preconditions.checkNotNull(source);
    }

    public SourceException(String message,StatementSourceReference source, Throwable cause) {
        super(message,cause);
        sourceRef = Preconditions.checkNotNull(source);
    }

    public StatementSourceReference getSourceReference() {
        return sourceRef;
    }

}
