/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@code binary} value.
 */
@NonNullByDefault
public abstract non-sealed class Binary implements ScalarValue {

    public abstract int size();

    public abstract byte[] toBytes();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);
}
