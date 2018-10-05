/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface describing YANG 'identity' statement.
 *
 * <p>
 * The 'identity' statement is used to define a new globally unique, abstract, and untyped identity. Its only purpose
 * is to denote its name, semantics, and existence. The built-in datatype "identityref" can be used to reference
 * identities within a data model.
 */
public interface IdentitySchemaNode extends SchemaNode {
    /**
     * Return base identities of this identity. The semantics of differ between RFC6020 and RFC7950 here. YANG 1.0
     * uses single inheritance, where there can be 0..1 base identities. YANG 1.1 uses multiple inheritance, where
     * there can be 0..N base identities.
     *
     * <p>
     * Callers should be prepared to handle multiple base identities.
     *
     * @return set of existing identities from which the new identity is derived or an empty Set if the identity is
     *         a root identity.
     */
    @NonNull Set<IdentitySchemaNode> getBaseIdentities();

    /**
     * Get identities derived from this identity.
     *
     * @return collection of identities derived from this identity
     */
    Set<IdentitySchemaNode> getDerivedIdentities();
}
