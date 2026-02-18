/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

/**
 * Helper interface aiding resolution of {@code type leafref} chains.
 */
@Beta
@FunctionalInterface
public interface LeafrefResolver {
    /**
     * Resolve specified {@link LeafrefTypeDefinition} until a non-{@code leafref} type is found.
     *
     * @param type leafref definition
     * @return Resolved type
     * @throws NullPointerException if {@code type} is null
     * @throws IllegalArgumentException if the type definition cannot be resolved
     */
    @NonNull TypeDefinition<?> resolveLeafref(@NonNull LeafrefTypeDefinition type);
}
