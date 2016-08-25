/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 *
 * Contains method for getting data from <code>identityref</code> built-in YANG
 * type.
 *
 */
@Value.Immutable
public interface IdentityrefTypeDefinition extends TypeDefinition<IdentityrefTypeDefinition> {

    /**
     * Returns identity to which the instance of this type refers.
     *
     * @return identity which is specified with the <code>identity</code> YANG
     *         statement
     */
    IdentitySchemaNode getIdentity();

}
