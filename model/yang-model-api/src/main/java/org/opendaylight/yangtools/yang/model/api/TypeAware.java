/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Marker interface for SchemaNodes which store simple data, for which we have type information.
 */
public interface TypeAware {
    /**
     * Returns the type definition of stored data.
     *
     * @return type definition.
     */
    @NonNull TypeDefinition<? extends TypeDefinition<?>> getType();
}
