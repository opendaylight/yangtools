/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

public final class ModuleEffectiveStatementImpl extends AbstractEffectiveModule<ModuleStatement> {
    private final QNameModule qnameModule;

    public ModuleEffectiveStatementImpl(
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        super(ctx);

        final QNameModule module = ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx);
        if (module.getRevision() == null) {
            qnameModule = QNameModule.create(module.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV).intern();
        } else {
            qnameModule = module;
        }
    }

    @Override
    public QNameModule getQNameModule() {
        return qnameModule;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(getYangVersion());
        result = prime * result + Objects.hashCode(qnameModule);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModuleEffectiveStatementImpl)) {
            return false;
        }
        ModuleEffectiveStatementImpl other = (ModuleEffectiveStatementImpl) obj;
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        if (!qnameModule.equals(other.qnameModule)) {
            return false;
        }
        if (!Objects.equals(getYangVersion(), other.getYangVersion())) {
            return false;
        }
        return true;
    }

}
