/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

@Beta
public class InvalidBitDefinitionException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    private final Bit offendingBit;

    protected InvalidBitDefinitionException(final Bit offendingBit, final String message) {
        super(message);
        this.offendingBit = Preconditions.checkNotNull(offendingBit);
    }

    public InvalidBitDefinitionException(final Bit offendingBit, final String format,
            final Object... args) {
        this(offendingBit, String.format(format, args));
    }

    public InvalidBitDefinitionException(final String format, Object... args) {
        super(String.format(format, args));
        this.offendingBit = null;
    }

    @Nullable
    public Bit getOffendingBit() {
        return offendingBit;
    }
}
