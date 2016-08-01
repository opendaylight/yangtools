/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import java.util.Date;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToSemVerModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class ImportEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, ImportStatement> implements
        ModuleImport {

    private static final Optional<Date> DEFAULT_REVISION = Optional.of(SimpleDateFormatUtil.DEFAULT_DATE_REV);

    private final String moduleName;
    private final Date revision;
    private final SemVer semVer;
    private final String prefix;

    public ImportEffectiveStatementImpl(final StmtContext<String, ImportStatement, ?> ctx) {
        super(ctx);

        moduleName = ctx.getStatementArgument();
        PrefixEffectiveStatementImpl prefixStmt = firstEffective(PrefixEffectiveStatementImpl.class);
        if (prefixStmt != null) {
            this.prefix = prefixStmt.argument();
        } else {
            final Optional<Date> revisionDate = Optional.fromNullable(
                    Utils.getLatestRevision(ctx.getRoot().declaredSubstatements())).or(DEFAULT_REVISION);
            final String formattedRevisionDate = SimpleDateFormatUtil.getRevisionFormat().format(revisionDate.get());
            final SourceIdentifier sourceId = RevisionSourceIdentifier.create(
                    (String) ctx.getRoot().getStatementArgument(), formattedRevisionDate);
            throw new MissingSubstatementException("Prefix is mandatory substatement of import statement",
                    ctx.getStatementSourceReference(), sourceId);
        }

        if (!ctx.isEnabledSemanticVersioning()) {
            RevisionDateEffectiveStatementImpl revisionDateStmt = firstEffective(RevisionDateEffectiveStatementImpl.class);
            this.revision = (revisionDateStmt == null) ? SimpleDateFormatUtil.DEFAULT_DATE_IMP : revisionDateStmt
                    .argument();
            this.semVer = Module.DEFAULT_SEMANTIC_VERSION;
        } else {
            ModuleIdentifier importedModuleIdentifier = ctx.getFromNamespace(ImpPrefixToSemVerModuleIdentifier.class, prefix);
            revision = importedModuleIdentifier.getRevision();
            semVer = importedModuleIdentifier.getSemanticVersion();
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
    public SemVer getSemanticVersion() {
        return semVer;
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
        result = prime * result + Objects.hashCode(semVer);
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
        if (!Objects.equals(getSemanticVersion(), other.getSemanticVersion())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ImportEffectiveStatementImpl.class.getSimpleName() + "[moduleName=" + moduleName + ", revision="
                + revision + ", semantic version=" + semVer + ", prefix=" + prefix + "]";
    }
}
