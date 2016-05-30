/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Utility {@link Identifier} backed by a {@link String}.
 */
@Beta
public abstract class StringIdentifier<T> implements Identifier, Comparable<StringIdentifier> {
    private static final long serialVersionUID = 1L;
    private final String string;

    public StringIdentifier(final String string) {
        this.string = Preconditions.checkNotNull(string);
    }

    public String getString() {
        return string;
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof StringIdentifier && string.equals(((StringIdentifier)o).string));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(StringIdentifier.class).add("string", string).toString();
    }

    @Override
    public int compareTo(final StringIdentifier o) {
        return string.compareTo(o.string);
    }
}
