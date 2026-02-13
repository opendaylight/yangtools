/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * A type-safe unique reference to a source. It has a {@link #correctId()} useful for at least diagnostic purposes, but
 * its most important trait is that it is identity-based. It can therefore be used to model relationships without the
 * fear of conflicts.
 *
 * <p>Instances can be obtained via {@link SourceInfo#newRef()}.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface SourceRef extends Immutable {
    /**
     * A {@link SourceRef} to a {@code module}.
     */
    sealed interface ToModule extends SourceRef permits ModuleRef {
        // Nothing else
    }

    /**
     * A {@link SourceRef} to a {@code submodule}.
     */
    sealed interface ToSubmodule extends SourceRef permits SubmoduleRef {
        // Nothing else
    }

    /**
     * {@return the accurate {@code SourceIdentifier} represented by this reference}
     */
    SourceIdentifier correctId();

    /**
     * {@inheritDoc}
     *
     * <p>The implementation is guaranteed to return {@code System.identityHashCode(this)}.
     */
    @Override
    int hashCode();

    /**
     * {@inheritDoc}
     *
     * <p>The implementation is guaranteed to return {@code (this == obj)}.
     */
    @Override
    boolean equals(@Nullable Object obj);
}
