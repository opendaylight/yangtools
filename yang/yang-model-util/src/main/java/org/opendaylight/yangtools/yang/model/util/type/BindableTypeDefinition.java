/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * A {@link TypeDefinition} which can be bound to a {@link TypeDefinition#getPath()}.
 *
 * @param <T> Concrete {@link TypeDefinition} type
 */
@Beta
public interface BindableTypeDefinition<T extends TypeDefinition<T>> extends TypeDefinition<T> {
    /**
     * Bind this type definition to a new path. The resulting definition will be equivalent to this definition in all
     * aspects except its path.
     *
     * @param newPath New path to use
     * @return Bound type definition
     */
    // FIXME: This should accept a @NonNull QName instead
    @NonNull BindableTypeDefinition<T> bindTo(SchemaPath newPath);
}
