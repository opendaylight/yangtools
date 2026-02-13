/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

/**
 * The combination of a {@link SourceInfo} and its corresponding {@link SourceRef}.
 * @since 15.0.0
 */
public sealed interface SourceInfoRef {
    /**
     * A {@link SourceInfoRef} to a {@code module}.
     */
    sealed interface OfModule extends SourceInfoRef permits ModuleInfoRef {
        @Override
        SourceInfo.Module info();

        @Override
        SourceRef.ToModule ref();
    }

    /**
     * A {@link SourceInfoRef} to a {@code submodule}.
     */
    sealed interface OfSubmodule extends SourceInfoRef permits SubmoduleInfoRef {
        @Override
        SourceInfo.Submodule info();

        @Override
        SourceRef.ToSubmodule ref();
    }

    /**
     * {@return the {@link SourceInfo}}
     */
    SourceInfo info();

    /**
     * {@return the {@link SourceRef}}
     */
    SourceRef ref();
}
