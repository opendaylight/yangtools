/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Interface describing YANG 'identity' statement.
 *
 * <p>
 * The 'identity' statement is used to define a new globally unique, abstract,
 * and untyped identity. Its only purpose is to denote its name, semantics, and
 * existence. The built-in datatype "identityref" can be used to reference
 * identities within a data model.
 */
public interface IdentitySchemaNode extends SchemaNode {
    /**
     * The YANG 1.0 (RFC6020) implementation of IdentitySchemaNode always returns an ImmutableSet containing just one
     * base identity or an empty ImmutableSet as it does not support multiple base identities.
     * Starting with YANG 1.1 (RFC7950), the identity can be derived from multiple base identities.
     *
     * @return set of existing identities from which the new identity is derived or
     *         an empty ImmutableSet if the identity is defined from scratch.
     */
    @Nonnull Set<IdentitySchemaNode> getBaseIdentities();

    /**
     * Get identities derived from this identity.
     *
     * @return collection of identities derived from this identity
     */
    Set<IdentitySchemaNode> getDerivedIdentities();

}
