/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code module} statement.
 */
public interface ModuleStatement extends MetaDeclaredStatement<Unqualified>,
        LinkageDeclaredStatement, RevisionAwareDeclaredStatement, BodyDeclaredStatement {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.MODULE;
    }

    default @Nullable YangVersionStatement getYangVersion() {
        final var opt = findFirstDeclaredSubstatement(YangVersionStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }

    default @NonNull NamespaceStatement getNamespace() {
        return findFirstDeclaredSubstatement(NamespaceStatement.class).orElseThrow();
    }

    default @NonNull PrefixStatement getPrefix() {
        return findFirstDeclaredSubstatement(PrefixStatement.class).orElseThrow();
    }
}
