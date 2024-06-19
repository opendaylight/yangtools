/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;

/**
 * This is not really a codec context, but rather a holder of input and output codec contexts.
 */
final class ActionCodecContext {
    private final DataObjectCodecContext<?, InputRuntimeType> input;
    private final DataObjectCodecContext<?, OutputRuntimeType> output;

    ActionCodecContext(final DataObjectCodecContext<?, InputRuntimeType> input,
            final DataObjectCodecContext<?, OutputRuntimeType> output) {
        this.input = requireNonNull(input);
        this.output = requireNonNull(output);
    }

    DataObjectCodecContext<?, InputRuntimeType> input() {
        return input;
    }

    DataObjectCodecContext<?, OutputRuntimeType> output() {
        return output;
    }
}
