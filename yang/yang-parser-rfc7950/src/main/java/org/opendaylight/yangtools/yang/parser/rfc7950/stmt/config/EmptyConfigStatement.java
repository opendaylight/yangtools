/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.config;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;

abstract class EmptyConfigStatement implements ConfigStatement {
    static final @NonNull ConfigStatement FALSE = new EmptyConfigStatement() {
        @Override
        public Boolean argument() {
            return Boolean.FALSE;
        }

        @Override
        ConfigEffectiveStatement toEffective() {
            return EmptyConfigEffectiveStatement.FALSE;
        }
    };

    static final @NonNull ConfigStatement TRUE = new EmptyConfigStatement() {
        @Override
        public Boolean argument() {
            return Boolean.TRUE;
        }

        @Override
        ConfigEffectiveStatement toEffective() {
            return EmptyConfigEffectiveStatement.TRUE;
        }
    };

    abstract ConfigEffectiveStatement toEffective();

    @Override
    public final Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return YangStmtMapping.CONFIG;
    }

    @Override
    public final String rawArgument() {
        return Boolean.toString(getValue());
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(statementDefinition(), getStatementSource(), argument(), rawArgument(),
            declaredSubstatements(), getValue());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConfigStatement)) {
            return false;
        }

        final ConfigStatement other = (ConfigStatement) obj;

        return getValue() == other.getValue() && argument().equals(other.argument())
                && rawArgument().equals(other.rawArgument())
                && statementDefinition().equals(other.statementDefinition())
                && getStatementSource().equals(other.getStatementSource())
                && declaredSubstatements().equals(other.declaredSubstatements());
    }
}
