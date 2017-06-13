/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import static org.opendaylight.yangtools.yang.parser.stmt.reactor.StmtContextUtils.firstAttributeOf;

import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;

public final class SubmoduleEffectiveStatementImpl extends AbstractEffectiveModule<SubmoduleStatement> {

    private final QNameModule qNameModule;

    public SubmoduleEffectiveStatementImpl(
            final StmtContext<String, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> ctx) {
        super(ctx);

        String belongsToModuleName = firstAttributeOf(ctx.declaredSubstatements(), BelongsToStatement.class);
        final QNameModule belongsToModuleQName = ctx.getFromNamespace(ModuleNameToModuleQName.class,
                belongsToModuleName);
        RevisionEffectiveStatementImpl submoduleRevision = firstEffective(RevisionEffectiveStatementImpl.class);

        this.qNameModule = (submoduleRevision == null ?
                QNameModule.create(belongsToModuleQName.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV) :
                    QNameModule.create(belongsToModuleQName.getNamespace(), submoduleRevision.argument())).intern();
    }

    @Override
    public QNameModule getQNameModule() {
        return qNameModule;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(getYangVersion());
        result = prime * result + Objects.hashCode(qNameModule);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SubmoduleEffectiveStatementImpl other = (SubmoduleEffectiveStatementImpl) obj;
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        if (!qNameModule.equals(other.qNameModule)) {
            return false;
        }
        if (!Objects.equals(getYangVersion(), other.getYangVersion())) {
            return false;
        }
        return true;
    }

}
