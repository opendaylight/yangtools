/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;

/**
 * A specialization of {@link AbstractInternedStatementSupport} for {@link String} arguments.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class AbstractInternedStringStatementSupport<D extends DeclaredStatement<String>,
        E extends EffectiveStatement<String, D>> extends AbstractInternedStatementSupport<String, D, E> {
    protected AbstractInternedStringStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<String, D> policy, final SubtreePolicy subtreePolicy,
            final YangParserConfiguration config, final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, subtreePolicy, config, validator);
    }

    protected AbstractInternedStringStatementSupport(final StatementDefinition publicDefinition,
            final StatementPolicy<String, D> policy, final YangParserConfiguration config,
            final @Nullable SubstatementValidator validator) {
        super(publicDefinition, policy, config, validator);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }
}
