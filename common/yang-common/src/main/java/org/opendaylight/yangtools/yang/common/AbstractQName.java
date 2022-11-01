/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.Serial;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * Abstract superclass for sharing QName references, which can either be resolved {@link QName}s or unresolved
 * {@link UnresolvedQName.Unqualified} and {@link UnresolvedQName.Qualified}s.
 */
@NonNullByDefault
public abstract sealed class AbstractQName implements Identifier, WritableObject permits QName, UnresolvedQName {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String localName;

    AbstractQName(final String localName) {
        this.localName = requireNonNull(localName);
    }

    /**
     * Returns YANG schema identifier which were defined for this node in the YANG module.
     *
     * @return YANG schema identifier which were defined for this node in the YANG module
     */
    public final String getLocalName() {
        return localName;
    }

    /**
     * Return an interned reference to an equivalent object.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public abstract AbstractQName intern();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public abstract String toString();

    /**
     * Returns a QName with the specified namespace and the same local name as this one.
     *
     * @param namespace New namespace to use
     * @return a QName with specified QNameModule and same local name as this one
     * @throws NullPointerException if namespace is null
     */
    public QName bindTo(final QNameModule namespace) {
        return new QName(namespace, getLocalName());
    }

    /**
     * Check whether a string is a valid {@code localName}.
     *
     * @param str String to check
     * @return True if the string usable as a local name, false otherwise
     */
    static final boolean isValidLocalName(final @Nullable String str) {
        return str != null && !str.isEmpty() && checkContent(str);
    }

    @Serial
    abstract Object writeReplace();

    static final String checkLocalName(final @Nullable String localName) {
        checkArgument(!localName.isEmpty(), "Parameter 'localName' must be a non-empty string.");
        checkArgument(checkContent(localName), "String '%s' is not a valid identifier", localName);
        return localName;
    }

    private static boolean checkContent(final String localName) {
        return YangNames.IDENTIFIER_START.matches(localName.charAt(0))
            && YangNames.NOT_IDENTIFIER_PART.indexIn(localName, 1) == -1;
    }
}
