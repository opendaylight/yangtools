/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import com.google.common.base.Verify;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;

final class ModuleEffectiveStatementImpl extends AbstractEffectiveModule<ModuleStatement>
        implements ModuleEffectiveStatement {
    private final QNameModule qnameModule;

    ModuleEffectiveStatementImpl(
            final StmtContext<String, ModuleStatement, EffectiveStatement<String, ModuleStatement>> ctx) {
        super(ctx);
        qnameModule = Verify.verifyNotNull(ctx.getFromNamespace(ModuleCtxToModuleQName.class, ctx));
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
