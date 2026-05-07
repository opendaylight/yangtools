/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Compatibility {@link GeneratedType} method implementations for {@link Archetype}s which do not provide them.
 */
public sealed interface GeneratedTypeCompat extends GeneratedType permits FeatureArchetype {
    /**
     * Returns a string that contains a human-readable textual description of
     * type definition.
     *
     * @return a human-readable textual description of type definition.
     */
    @Override
    @Deprecated(forRemoval = true)
    default String getDescription() {
        throw uoe();
    }

    /**
     * Returns a string that is used to specify a textual cross-reference to an
     * external document, either another module that defines related management
     * information, or a document that provides additional information relevant
     * to this definition.
     *
     * @return a textual cross-reference to an external document.
     */
    @Override
    @Deprecated(forRemoval = true)
    default String getReference() {
        throw uoe();
    }

    /**
     * Returns the name of the module, in which generated type was specified.
     *
     * @return the name of the module, in which generated type was specified.
     */
    @Override
    @Deprecated(forRemoval = true)
    default String getModuleName() {
        throw uoe();
    }

    @Override
    @Deprecated(forRemoval = true)
    default @Nullable TypeComment getComment() {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        throw new UnsupportedOperationException("should never be called");
    }
}
