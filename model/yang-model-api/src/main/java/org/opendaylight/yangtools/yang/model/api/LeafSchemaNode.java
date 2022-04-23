/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

/**
 * Interface describing YANG {@code leaf} statement. The 'leaf' statement is used to define a leaf node in the schema
 * tree.
 *
 * <p>
 * Since we are presenting the effective model of the world, the information dictated by 'default' and 'units'
 * substatements is captured in the type returned via {@link #getType()}.
 */
public non-sealed interface LeafSchemaNode extends TypedDataSchemaNode, MandatoryAware, MustConstraintAware,
        EffectiveStatementEquivalent {
    @Override
    LeafEffectiveStatement asEffectiveStatement();
}
