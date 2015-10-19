/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Date;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ImportEffectiveStatementImpl extends EffectiveStatementBase<String, ImportStatement> implements
        ModuleImport {
    private static final long serialVersionUID = 1L;

    private final String moduleName;
    private final Date revision;
    private final String prefix;

    public ImportEffectiveStatementImpl(final StmtContext<String, ImportStatement, ?> ctx) {
        super(ctx);

        moduleName = ctx.getStatementArgument();

        RevisionDateEffectiveStatementImpl revisionDateStmt = firstEffective(RevisionDateEffectiveStatementImpl.class);
        this.revision = (revisionDateStmt == null) ? SimpleDateFormatUtil.DEFAULT_DATE_IMP : revisionDateStmt.argument();

        PrefixEffectiveStatementImpl prefixStmt = firstEffective(PrefixEffectiveStatementImpl.class);
        if (prefixStmt != null ) {
            this.prefix = prefixStmt.argument();
        } else {
            throw new IllegalStateException("Prefix is mandatory substatement of import statement");
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
        result = prime * result + Objects.hashCode(moduleName);
        result = prime * result + Objects.hashCode(revision);
        result = prime * result + Objects.hashCode(prefix);
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
        ImportEffectiveStatementImpl other = (ImportEffectiveStatementImpl) obj;
        if (!Objects.equals(getModuleName(), other.getModuleName())) {
            return false;
        }
        if (!Objects.equals(getRevision(), other.getRevision())) {
            return false;
        }
        if (!Objects.equals(getPrefix(), other.getPrefix())) {
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
