/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;

final class SubmoduleEffectiveStatementImpl extends AbstractEffectiveModule<SubmoduleStatement>
        implements SubmoduleEffectiveStatement {

    private final QNameModule qnameModule;

    SubmoduleEffectiveStatementImpl(final StmtContext<String, SubmoduleStatement, SubmoduleEffectiveStatement> ctx) {
        super(ctx);

        final String belongsToModuleName = firstAttributeOf(ctx.declaredSubstatements(), BelongsToStatement.class);
        final QNameModule belongsToModuleQName = ctx.getFromNamespace(ModuleNameToModuleQName.class,
                belongsToModuleName);

        final Optional<Revision> submoduleRevision = findFirstEffectiveSubstatementArgument(
            RevisionEffectiveStatement.class);
        this.qnameModule = QNameModule.create(belongsToModuleQName.getNamespace(), submoduleRevision).intern();
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getYangVersion(), qnameModule);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SubmoduleEffectiveStatementImpl)) {
            return false;
        }
        final SubmoduleEffectiveStatementImpl other = (SubmoduleEffectiveStatementImpl) obj;
        return Objects.equals(getName(), other.getName()) && qnameModule.equals(other.qnameModule)
                && Objects.equals(getYangVersion(), other.getYangVersion());
    }
}
