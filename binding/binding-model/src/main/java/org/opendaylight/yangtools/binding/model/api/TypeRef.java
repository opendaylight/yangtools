/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.impl.TypeRefImpl;

/**
 * A {@link Type} which is a plain reference with no other implications.
 *
 * @since 15.0.0
 * @deprecated This contract is a remnant of when we needed to reference any old {@link TypeName} as a {@link Type} and
 *             should not be used.
 */
@SuppressWarnings("removal")
@Deprecated(since = "16.0.0", forRemoval = true)
@NonNullByDefault
public sealed interface TypeRef extends Type permits TypeRefImpl {
    /**
     * {@return a {@link TypeRef} with specified name}
     * @param name the type name
     */
    @Deprecated(since = "16.0.0", forRemoval = true)
    static TypeRef of(final TypeName name) {
        return new TypeRefImpl(name);
    }
}
