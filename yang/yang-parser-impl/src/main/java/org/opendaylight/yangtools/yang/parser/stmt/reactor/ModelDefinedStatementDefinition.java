/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.UnknownStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

// FIXME: Provide real argument name
final class ModelDefinedStatementDefinition implements StatementDefinition {
    private final QName qName;

    ModelDefinedStatementDefinition(QName qName) {
        this.qName = qName;
    }

    @Nonnull
    @Override
    public QName getStatementName() {
        return qName;
    }

    @Nullable
    @Override
    public QName getArgumentName() {
        return qName;
    }

    @Nonnull
    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return UnknownStatementImpl.class;
    }

    @Nonnull
    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return UnknownEffectiveStatementImpl.class;
    }
}