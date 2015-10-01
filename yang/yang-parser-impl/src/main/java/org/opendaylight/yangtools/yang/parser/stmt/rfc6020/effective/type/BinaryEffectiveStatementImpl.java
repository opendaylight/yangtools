/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.util.BinaryType;

public final class BinaryEffectiveStatementImpl extends AbstractBuiltInTypeEffectiveStatement<BinaryTypeDefinition>
        implements BinaryTypeDefinition {
    private static final BinaryEffectiveStatementImpl INSTANCE = new BinaryEffectiveStatementImpl();

    private BinaryEffectiveStatementImpl() {

    }

    public static EffectiveStatement<String, TypeStatement> getInstance() {
        return INSTANCE;
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return delegate().getLengthConstraints();
    }

    @Override
    protected BinaryTypeDefinition delegate() {
        return BinaryType.getInstance();
    }
}
