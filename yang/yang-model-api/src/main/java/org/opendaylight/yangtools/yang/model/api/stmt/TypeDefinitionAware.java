/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Interface indicating an entity which acts as a holder of a {@link TypeDefinition}.
 */
@Beta
public interface TypeDefinitionAware {
    /**
     * Return this statement's effective type definition.
     *
     * @return Effective {@link TypeDefinition} as defined by this statement.
     */
    @NonNull TypeDefinition<?> getTypeDefinition();
}
