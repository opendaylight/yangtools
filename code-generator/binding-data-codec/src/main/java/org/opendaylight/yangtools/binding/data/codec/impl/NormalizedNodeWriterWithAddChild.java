package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

final class NormalizedNodeWriterWithAddChild extends ImmutableNormalizedNodeStreamWriter {

    NormalizedNodeWriterWithAddChild(final NormalizedNodeResult result) {
        super(result);
    }

    void addChild(final NormalizedNode<?, ?> child) {
        this.writeChild(child);
    }
}
