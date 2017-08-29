/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public final class InvalidLengthConstraintException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidLengthConstraintException(final @NonNull String format, final Object... args) {
        super(String.format(format, args));
    }
}
