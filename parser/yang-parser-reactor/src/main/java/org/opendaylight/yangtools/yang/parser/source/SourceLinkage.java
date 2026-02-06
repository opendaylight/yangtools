/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A single linked source.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface SourceLinkage permits ModuleLinkage, SubmoduleLinkage {
    /**
     * A reference to a {@link SourceLinkage}. It has a {@link #sourceId()}, diagnostic purposes, but its most important
     * trait is that it is identity-based.
     */
    sealed interface Ref extends Immutable permits ModuleRef, SubmoduleRef {

        SourceIdentifier sourceId();
    }

    /**
     * A {@link Ref} to a {@link ModuleLinkage}.
     */
    record ModuleRef(SourceIdentifier sourceId) implements Ref {
        public ModuleRef {
            requireNonNull(sourceId);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("hash", Integer.toHexString(hashCode()))
                .add("sourceId", sourceId)
                .toString();
        }
    }

    /**
     * A {@link Ref} to a {@link SubmoduleLinkage}.
     */
    record SubmoduleRef(SourceIdentifier sourceId) implements Ref {
        public SubmoduleRef {
            requireNonNull(sourceId);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("hash", Integer.toHexString(hashCode()))
                .add("sourceId", sourceId)
                .toString();
        }
    }

    Ref ref();

    Unqualified name();

    YangVersion version();

    Map<Unqualified, ModuleRef> imports();

    Map<Unqualified, SubmoduleRef> includes();

    // FIXME: also stream provider at some point?
}
