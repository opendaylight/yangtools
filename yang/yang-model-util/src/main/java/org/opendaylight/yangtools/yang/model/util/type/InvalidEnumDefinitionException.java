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
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

@Beta
public class InvalidEnumDefinitionException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;
    private final EnumPair offendingEnum;

    protected InvalidEnumDefinitionException(final EnumPair offendingEnum, final String message) {
        super(message);
        this.offendingEnum = Preconditions.checkNotNull(offendingEnum);
    }

    public InvalidEnumDefinitionException(final EnumPair offendingEnum, final String format,
            final Object... args) {
        this(offendingEnum, String.format(format, args));
    }

    public EnumPair getOffendingEnum() {
        return offendingEnum;
    }
}
