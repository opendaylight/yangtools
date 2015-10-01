/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.util.Uint64;

public final class UInt64EffectiveStatementImpl extends AbstractUnsignedIntegerBuiltInTypeEffectiveStatement {
    private static final UInt64EffectiveStatementImpl INSTANCE = new UInt64EffectiveStatementImpl();

    private UInt64EffectiveStatementImpl() {

    }

    public static UInt64EffectiveStatementImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    protected Uint64 delegate() {
        return Uint64.getInstance();
    }

    @Override
    public TypeEffectiveStatement<TypeStatement> derive(final EffectiveStatement<?, TypeStatement> stmt, final SchemaPath path) {
        return new DerivedUnsignedIntegerEffectiveStatement(stmt, path, this);
    }
}
