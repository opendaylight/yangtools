/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.io.DataInput;
import java.io.IOException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Abstract base class for versions which can produce legacy versions of PathArgument.
 */
abstract class AbstractLegacyDataInput extends AbstractNormalizedNodeDataInput {
    AbstractLegacyDataInput(final DataInput input) {
        super(input);
    }

    @Override
    public final PathArgument readPathArgument() throws IOException {
        final var legacy = readLegacyPathArgument();
        if (legacy.isFirst()) {
            return legacy.getFirst();
        }
        throw new InvalidNormalizedNodeStreamException(legacy.getSecond() + " does not have a representation");
    }
}
