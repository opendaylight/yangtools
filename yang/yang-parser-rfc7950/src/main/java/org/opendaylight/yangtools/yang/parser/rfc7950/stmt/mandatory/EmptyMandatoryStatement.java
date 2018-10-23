/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.mandatory;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;

abstract class EmptyMandatoryStatement implements MandatoryStatement {
    static final @NonNull MandatoryStatement FALSE = new EmptyMandatoryStatement() {
        @Override
        public Boolean argument() {
            return Boolean.FALSE;
        }

        @Override
        EffectiveStatement<Boolean, MandatoryStatement> toEffective() {
            return EmptyMandatoryEffectiveStatement.FALSE;
        }
    };

    static final @NonNull MandatoryStatement TRUE = new EmptyMandatoryStatement() {
        @Override
        public Boolean argument() {
            return Boolean.TRUE;
        }

        @Override
        EffectiveStatement<Boolean, MandatoryStatement> toEffective() {
            return EmptyMandatoryEffectiveStatement.TRUE;
        }
    };

    abstract EffectiveStatement<Boolean, MandatoryStatement> toEffective();

    @Override
    public final Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return ImmutableList.of();
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return YangStmtMapping.MANDATORY;
    }

    @Override
    public final String rawArgument() {
        return getValue().toString();
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(statementDefinition(), getStatementSource(), argument(),
            rawArgument(), declaredSubstatements(), getValue());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MandatoryStatement)) {
            return false;
        }

        final MandatoryStatement other = (MandatoryStatement) obj;
        return argument().equals(other.argument())
                && rawArgument().equals(other.rawArgument())
                && getValue().equals(other.getValue())
                && statementDefinition().equals(other.statementDefinition())
                && getStatementSource().equals(other.getStatementSource())
                && declaredSubstatements().equals(other.declaredSubstatements());
    }
}
