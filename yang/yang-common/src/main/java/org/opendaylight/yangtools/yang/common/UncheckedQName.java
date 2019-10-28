/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An unchecked QName. This class is probably useless outside of caching.
 */
@Beta
@NonNullByDefault
public final class UncheckedQName implements Immutable {
    private final QNameModule module;
    private final String localName;

    private UncheckedQName(final QNameModule module, final String localName) {
        this.module = requireNonNull(module);
        this.localName = requireNonNull(localName);
    }

    public static UncheckedQName of(final QNameModule module, final String localName) {
        return new UncheckedQName(module, localName);
    }

    public QNameModule getModule() {
        return module;
    }

    public String getLocalName() {
        return localName;
    }

    public QName toQName() {
        return QName.create(module, localName);
    }

    @Override
    public int hashCode() {
        return 31 * module.hashCode() + localName.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UncheckedQName)) {
            return false;
        }
        final UncheckedQName other = (UncheckedQName) obj;
        return localName.equals(other.localName) && module.equals(other.module);
    }
}
