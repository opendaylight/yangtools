package org.opendaylight.yangtools.yang.data.impl.schema;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class ResultAlreadySetException extends IllegalStateException {
    private final NormalizedNode<?, ?> resultData;

    public ResultAlreadySetException(String message, NormalizedNode<?, ?> resultData) {
        this(message, resultData, null);
    }

    public ResultAlreadySetException(String message, NormalizedNode<?, ?> resultData, Throwable cause) {
        super(message, cause);
        this.resultData = resultData;
    }

    public NormalizedNode<?, ?> getResultData() {
        return resultData;
    }
}
