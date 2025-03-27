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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A opportunistically-caching {@link DerivedString}. Canonical name is cached at first encounter.
 *
 * @param <T> derived string type
 */
@Beta
@NonNullByDefault
public abstract class CachingDerivedString<T extends CachingDerivedString<T>> extends DerivedString<T> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final VarHandle STR;

    static {
        try {
            STR = MethodHandles.lookup().findVarHandle(CachingDerivedString.class, "str", String.class);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    private transient volatile @Nullable String str;

    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "@NonNullByDefault vs @Nullable field")
    private CachingDerivedString(final @Nullable Void dummy) {
        str = null;
    }

    protected CachingDerivedString() {
        this((Void) null);
    }

    protected CachingDerivedString(final String str) {
        this.str = requireNonNull(str);
    }

    @Override
    public final String toCanonicalString() {
        final var local = (String) STR.getAcquire(this);
        return local != null ? local : loadCanonicalString();
    }

    private String loadCanonicalString() {
        final var computed = computeCanonicalString();
        final var witness = (String) STR.compareAndExchangeRelease(this, null, computed);
        return witness != null ? witness : computed;
    }

    /**
     * Return the canonical string representation of this object's value.
     *
     * @return Canonical string
     */
    protected abstract String computeCanonicalString();
}
