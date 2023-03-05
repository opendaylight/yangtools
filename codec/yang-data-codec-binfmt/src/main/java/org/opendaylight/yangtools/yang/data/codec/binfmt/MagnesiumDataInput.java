/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import java.io.DataInput;
import java.io.IOException;
import java.math.BigInteger;
import org.opendaylight.yangtools.yang.data.spi.value.ValueInterner;

final class MagnesiumDataInput extends AbstractMagnesiumDataInput {
    MagnesiumDataInput(final DataInput input, final ValueInterner interner) {
        super(input, interner);
    }

    @Override
    public NormalizedNodeStreamVersion getVersion() {
        return NormalizedNodeStreamVersion.MAGNESIUM;
    }

    @Override
    BigInteger readBigInteger() throws IOException {
        throw new InvalidNormalizedNodeStreamException("BigInteger coding is not supported");
    }
}
