/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link Type} which is a plain reference with no other implications.
 *
 * @since 15.0.0
 */
@NonNullByDefault
public sealed interface TypeRef extends Type permits DefaultTypeRef {
    /**
     * {@return a {@link TypeRef} with specified name}
     * @param name the type name
     */
    static TypeRef of(final JavaTypeName name) {
        return new DefaultTypeRef(name);
    }
}
