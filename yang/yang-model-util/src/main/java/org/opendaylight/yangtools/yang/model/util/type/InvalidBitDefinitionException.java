/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

@Beta
public class InvalidBitDefinitionException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings("SE_BAD_FIELD")
    private final Bit offendingBit;

    protected InvalidBitDefinitionException(final Bit offendingBit, final String message) {
        super(message);
        this.offendingBit = requireNonNull(offendingBit);
    }

    public InvalidBitDefinitionException(final Bit offendingBit, final String format,
            final Object... args) {
        this(offendingBit, String.format(format, args));
    }

    public Bit getOffendingBit() {
        return offendingBit;
    }
}
