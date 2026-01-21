/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

/**
 * Common interface for {@link DeclaredStatement}s whose argument represents a reference to a schema node. This
 * includes, for example, {@code augment}, {@code deviation} and {@code refine} statements.
 *
 * @param <A> argument type
 * @see EffectiveSchemaTreeReferenceTextStatement
 */
public interface DeclaredSchemaTreeReferenceStatement<A extends SchemaNodeIdentifier> extends DeclaredStatement<A> {
    // Nothing else
}
