/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Identifier;

@Beta
public final class StringIdentifier implements Identifier, Comparable<StringIdentifier> {
    private static final long serialVersionUID = 1L;
    private final String str;

    private StringIdentifier(final String str) {
        this.str = Preconditions.checkNotNull(str);
    }

    public static StringIdentifier valueOf(final String str) {
        return new StringIdentifier(str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || (o instanceof StringIdentifier && str.equals(((StringIdentifier)o).str));
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public int compareTo(final StringIdentifier o) {
        return str.compareTo(o.str);
    }
}
