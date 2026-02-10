/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Interface indicating an entity which acts as a holder of a {@link TypeDefinition}.
 */
@NonNullByDefault
public interface TypeDefinitionCompat {
    /**
     * {@return the effective {@link QNameModule} of this statement}
     */
    QNameModule currentModule();

    /**
     * {@return the effective {@link TypeDefinition} as defined by this statement}
     */
    TypeDefinition<?> typeDefinition();
}
