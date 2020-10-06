/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public interface ModuleStatement extends MetaDeclaredStatement<UnqualifiedQName>, ModuleHeaderGroup,
        LinkageDeclaredStatement, RevisionAwareDeclaredStatement, BodyDeclaredStatement {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.MODULE;
    }

    default @NonNull String getName() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(rawArgument());
    }

    @Override
    default YangVersionStatement getYangVersion() {
        final Optional<YangVersionStatement> opt = findFirstDeclaredSubstatement(YangVersionStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default NamespaceStatement getNamespace() {
        return findFirstDeclaredSubstatement(NamespaceStatement.class).get();
    }

    @Override
    default PrefixStatement getPrefix() {
        return findFirstDeclaredSubstatement(PrefixStatement.class).get();
    }
}
