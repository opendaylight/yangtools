/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

@Beta
public final class ModelDefinedStatementDefinition implements StatementDefinition {
    private final QName qName;
    private final boolean yinElement;

    ModelDefinedStatementDefinition(final QName qName, final boolean yinElement) {
        this.qName = Preconditions.checkNotNull(qName);
        this.yinElement = yinElement;
    }

    @Deprecated
    public ModelDefinedStatementDefinition(final QName qName) {
        this(qName, false);
    }

    @Nonnull
    @Override
    public QName getStatementName() {
        return qName;
    }

    @Nullable
    @Override
    public QName getArgumentName() {
        // FIXME: Provide real argument name
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

    @Override
    public boolean isArgumentYinElement() {
        return yinElement;
    }
}