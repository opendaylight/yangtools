/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;

/**
 * This is not really a codec context, but rather a holder of input and output codec contexts.
 */
final class ActionCodecContext {
    private final DataContainerCodecContext<?, InputSchemaNode> input;
    private final DataContainerCodecContext<?, OutputSchemaNode> output;

    ActionCodecContext(final DataContainerCodecContext<?, InputSchemaNode> input,
        final DataContainerCodecContext<?, OutputSchemaNode> output) {
        this.input = requireNonNull(input);
        this.output = requireNonNull(output);
    }

    DataContainerCodecContext<?, InputSchemaNode> input() {
        return input;
    }

    DataContainerCodecContext<?, OutputSchemaNode> output() {
        return output;
    }
}
