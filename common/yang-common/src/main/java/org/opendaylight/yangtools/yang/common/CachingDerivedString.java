/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A opportunistically-caching {@link DerivedString}. Canonical name is cached at first encounter.
 *
 * @param <T> derived string type
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class CachingDerivedString<T extends CachingDerivedString<T>> extends DerivedString<T> {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient volatile @Nullable String str;

    @SuppressFBWarnings("NP_STORE_INTO_NONNULL_FIELD")
    protected CachingDerivedString() {
        this.str = null;
    }

    protected CachingDerivedString(final String str) {
        this.str = requireNonNull(str);
    }

    @Override
    public final String toCanonicalString() {
        String local;
        return (local = this.str) != null ? local : (str = computeCanonicalString());
    }

    /**
     * Return the canonical string representation of this object's value.
     *
     * @return Canonical string
     */
    protected abstract String computeCanonicalString();
}
