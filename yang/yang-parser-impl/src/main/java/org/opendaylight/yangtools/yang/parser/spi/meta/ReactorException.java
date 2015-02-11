package org.opendaylight.yangtools.yang.parser.spi.meta;


public class ReactorException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ModelProcessingPhase phase;

    public ReactorException(ModelProcessingPhase phase, String message, Throwable cause) {
        super(message, cause);
        this.phase = phase;
    }

    public ReactorException(ModelProcessingPhase phase, String message) {
        super(message);
        this.phase = phase;
    }

    public final ModelProcessingPhase getPhase() {
        return phase;
    }



}
