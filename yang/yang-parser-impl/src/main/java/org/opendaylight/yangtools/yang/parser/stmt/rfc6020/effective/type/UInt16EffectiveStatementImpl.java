/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.Uint16;

public final class UInt16EffectiveStatementImpl extends AbstractUnsignedIntegerBuiltInEffectiveStatement {
    private static final UInt16EffectiveStatementImpl INSTANCE = new UInt16EffectiveStatementImpl();

    private UInt16EffectiveStatementImpl() {

    }

    public static UInt16EffectiveStatementImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return Uint16.getInstance();
    }
}
