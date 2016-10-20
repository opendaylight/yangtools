/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
    private static final LoadingCache<QName, ModelDefinedStatementDefinition> CACHE = CacheBuilder.newBuilder()
            .weakValues().build(new CacheLoader<QName, ModelDefinedStatementDefinition>() {
                @Override
                public ModelDefinedStatementDefinition load(final QName key) throws Exception {
                    return new ModelDefinedStatementDefinition(key);
                }
            });
    private final QName qName;
    private final boolean yinElement;

    ModelDefinedStatementDefinition(final QName qName) {
        this(qName, false);
    }

    ModelDefinedStatementDefinition(final QName qName, final boolean yinElement) {
        this.qName = Preconditions.checkNotNull(qName);
        this.yinElement = yinElement;
    }

    static ModelDefinedStatementDefinition forQName(final QName key) {
        return CACHE.getUnchecked(key);
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

    @Override
    public boolean isArgumentYinElement() {
        return yinElement;
    }
}