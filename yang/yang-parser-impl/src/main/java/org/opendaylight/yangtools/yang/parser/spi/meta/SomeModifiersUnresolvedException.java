package org.opendaylight.yangtools.yang.parser.spi.meta;



public class SomeModifiersUnresolvedException extends ReactorException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SomeModifiersUnresolvedException(ModelProcessingPhase phase) {
        super(phase,"Some of " + phase + " modifiers for statements were not resolved.");
    }

}
