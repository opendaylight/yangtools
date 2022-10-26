/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

/**
 * Declared representation of a {@code refine} statement.
 */
public interface RefineStatement extends ConfigStatementAwareDeclaredStatement<Descendant>,
        DocumentedDeclaredStatement<Descendant>, IfFeatureAwareDeclaredStatement<Descendant>,
        MandatoryStatementAwareDeclaredStatement<Descendant>,
        MustStatementAwareDeclaredStatement<Descendant> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.REFINE;
    }

    default @NonNull Collection<? extends DefaultStatement> getDefaults() {
        return declaredSubstatements(DefaultStatement.class);
    }

    default @Nullable PresenceStatement getPresence() {
        final var opt = findFirstDeclaredSubstatement(PresenceStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }

    default @Nullable MinElementsStatement getMinElements() {
        final var opt = findFirstDeclaredSubstatement(MinElementsStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }

    default @Nullable MaxElementsStatement getMaxElements() {
        final var opt = findFirstDeclaredSubstatement(MaxElementsStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
