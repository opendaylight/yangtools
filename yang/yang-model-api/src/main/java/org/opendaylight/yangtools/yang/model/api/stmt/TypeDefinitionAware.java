/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;

/**
 * Interface indicating an entity is able to instantiate {@link TypeDefinitionBuilder} instances.
 */
@Beta
public interface TypeDefinitionAware {
    /**
     * Returns a {@link TypeDefinitionBuilder} for the type defined by this statement.
     *
     * @return A {@link TypeDefinitionBuilder} instance.
     */
    @Nonnull TypeDefinitionBuilder<?> newTypeDefinitionBuilder();
}
