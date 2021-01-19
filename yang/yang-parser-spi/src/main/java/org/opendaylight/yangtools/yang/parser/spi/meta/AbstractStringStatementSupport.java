/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Specialization of {@link AbstractStatementSupport} for String statement arguments. Note this (mostly) implies
 * context-independence.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class AbstractStringStatementSupport<D extends DeclaredStatement<String>,
        E extends EffectiveStatement<String, D>> extends AbstractStatementSupport<String, D, E> {
    protected AbstractStringStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<String, D> policy) {
        super(publicDefinition, policy);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }
}
