/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;

import java.util.Date;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ImportEffectiveStatementImpl extends EffectiveStatementBase<String, ImportStatement> implements
        ModuleImport {

    private String moduleName;
    private Date revision;
    private String prefix;

    public ImportEffectiveStatementImpl(StmtContext<String, ImportStatement, ?> ctx) {
        super(ctx);

        moduleName = ctx.getStatementArgument();
        revision = SimpleDateFormatUtil.DEFAULT_DATE_IMP;

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof RevisionDateEffectiveStatementImpl) {
                revision = ((RevisionDateEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof PrefixEffectiveStatementImpl) {
                prefix = ((PrefixEffectiveStatementImpl) effectiveStatement).argument();
            }
        }
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Date getRevision() {
        return revision;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImportEffectiveStatementImpl other = (ImportEffectiveStatementImpl) obj;
        if (getModuleName() == null) {
            if (other.getModuleName() != null) {
                return false;
            }
        } else if (!getModuleName().equals(other.getModuleName())) {
            return false;
        }
        if (getRevision() == null) {
            if (other.getRevision() != null) {
                return false;
            }
        } else if (!getRevision().equals(other.getRevision())) {
            return false;
        }
        if (getPrefix() == null) {
            if (other.getPrefix() != null) {
                return false;
            }
        } else if (!getPrefix().equals(other.getPrefix())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ImportEffectiveStatementImpl.class.getSimpleName() + "[moduleName=" + moduleName + ", revision="
                + revision + ", prefix=" + prefix + "]";
    }
}
