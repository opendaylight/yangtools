/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

final class DerivedEffectiveStatement<S extends TypeStatement, T extends TypeDefinition<T>> extends AbstractTypeEffectiveStatement<S, T>
    implements TypeDefinition<T> {

    DerivedEffectiveStatement(final EffectiveStatement<?, S> stmt, final SchemaPath path,
            final T baseType) {
        super(stmt, path, baseType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TypeEffectiveStatement<S> derive(final EffectiveStatement<?, S> stmt, final SchemaPath path) {
        return new DerivedEffectiveStatement<S, T>(stmt, path, (T)this);
    }
}
