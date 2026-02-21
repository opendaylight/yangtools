/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Marker interface which assign to object property that it is a bounded wildcard type.
 */
@NonNullByDefault
public sealed interface WildcardType extends Type permits DefaultWildcardType {
    /**
     * {@return a {@link WildcardType} for specified type name}
     * @param name the name
     */
    static WildcardType ofName(final JavaTypeName name) {
        return new DefaultWildcardType(name);
    }
}
