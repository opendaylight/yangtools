/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An unresolved {@link QName}. It is guaranteed to hold a valid {@link #getLocalName()}, but it lacks a namespace.
 * It can be resolved into a {@link QName} by supplying a namespace to {@link #bindTo(QNameModule)}.
 */
@Beta
@NonNullByDefault
public final class UnboundQName extends AbstractQName {
    private static final long serialVersionUID = 1L;
    private static final Interner<UnboundQName> INTERNER = Interners.newWeakInterner();

    private UnboundQName(final String localName) {
        super(localName);
    }

    public static UnboundQName of(final String localName) {
        return new UnboundQName(checkLocalName(localName));
    }

    public UnboundQName intern() {
        return INTERNER.intern(this);
    }

    public QName bindTo(final QNameModule namespace) {
        return new QName(namespace, getLocalName());
    }

    @Override
    public int hashCode() {
        return getLocalName().hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof UnboundQName
                && getLocalName().equals(((AbstractQName) obj).getLocalName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("localName", getLocalName()).toString();
    }
}
