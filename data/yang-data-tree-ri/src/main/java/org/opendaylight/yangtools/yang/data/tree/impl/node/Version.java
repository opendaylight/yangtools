/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.node;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.tree.api.VersionInfo;

/**
 * The concept of a version, either node version, or a subtree version. The only interface contract this class has is
 * that no two {@link Version} are the same.
 *
 * <p>
 * This class relies on Java Virtual machine's guarantee that the identity of an Object is distinct from any other
 * Object in the Java heap.
 *
 * <p>
 * From data management perspective, this concept serves as JVM-level MVCC
 * <a href="https://en.wikipedia.org/wiki/Multiversion_concurrency_control#Implementation">timestamp (TS)</a>.
 */
public sealed class Version {
    private static final class WithInfo extends Version {
        private static final @NonNull VersionInfo NULL = new VersionInfo() {
            // Nothing here: represents the effect of writeInfo(null)
        };
        private static final VarHandle VH;

        static {
            try {
                VH = MethodHandles.lookup().findVarHandle(WithInfo.class, "info", VersionInfo.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @SuppressWarnings("unused")
        private volatile VersionInfo info;

        @Override
        public WithInfo next() {
            return new WithInfo();
        }

        @Override
        public VersionInfo readInfo() {
            return unmaskNull((VersionInfo) VH.getAcquire(this));
        }

        @Override
        public VersionInfo writeInfo(final VersionInfo newInfo) {
            final var witness = (VersionInfo) VH.compareAndExchangeRelease(this, null, maskNull(newInfo));
            return witness == null ? newInfo : unmaskNull(witness);
        }

        private static @NonNull VersionInfo maskNull(final @Nullable VersionInfo info) {
            return info != null ? info : NULL;
        }

        private static @Nullable VersionInfo unmaskNull(final @Nullable VersionInfo info) {
            return info != NULL ? info : null;
        }
    }

    private Version() {
        // Hidden on purpose
    }

    /**
     * Create an initial version.
     *
     * @param trackInfo
     * @return a new version.
     */
    public static final @NonNull Version initial(final boolean trackInfo) {
        return trackInfo ? new WithInfo() : new Version();
    }

    /**
     * Create a new version, distinct from any other version.
     *
     * @return a new version.
     */
    public @NonNull Version next() {
        return new Version();
    }

    public @Nullable VersionInfo readInfo() {
        return null;
    }

    public @Nullable VersionInfo writeInfo(final @Nullable VersionInfo newInfo) {
        return null;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        final var sb = new StringBuilder("Version[").append(Integer.toHexString(hashCode()));
        final var info = readInfo();
        if (info != null) {
            sb.append(", ").append(info);
        }
        return sb.append(']').toString();
    }
}
