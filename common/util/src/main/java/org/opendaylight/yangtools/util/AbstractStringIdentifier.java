/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import java.util.UUID;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Utility {@link Identifier} backed by a {@link UUID}.
 */
public abstract class AbstractStringIdentifier<T extends AbstractStringIdentifier<T>>
        extends AbstractIdentifier<String> implements Comparable<T> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    protected AbstractStringIdentifier(final @NonNull String string) {
        super(string);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final T o) {
        return getValue().compareTo(o.getValue());
    }
}
