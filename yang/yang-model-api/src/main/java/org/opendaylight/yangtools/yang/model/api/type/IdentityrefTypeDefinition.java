/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.Set;
import javax.annotation.Nonnull;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Contains method for getting data from <code>identityref</code> built-in YANG
 * type.
 */
public interface IdentityrefTypeDefinition extends TypeDefinition<IdentityrefTypeDefinition> {
    /**
     * Returns identity to which the instance of this type refers.
     *
     * @deprecated use {@link #getIdentities()} instead
     *
     * @return identity which is specified with the <code>identity</code> YANG
     *         statement
     */
    @Deprecated
    IdentitySchemaNode getIdentity();

    /**
     * Returns the set of identities this reference points to.
     *
     * @return set of identities to which the instance of this type refers (in YANG 1.1 models) or a set containing
     *         just one identity (in YANG 1.0 models)
     */
    @Nonnull Set<IdentitySchemaNode> getIdentities();

    /**
     * Returns the module where the default value is defined.
     *
     * @return module where the default value (if any) of this type is defined.
     */
    QNameModule getDefaultValueModule();
}
