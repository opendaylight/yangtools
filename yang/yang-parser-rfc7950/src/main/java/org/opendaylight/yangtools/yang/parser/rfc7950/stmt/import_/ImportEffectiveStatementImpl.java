/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionDateEffectiveStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNodeWithoutStatus;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToSemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class ImportEffectiveStatementImpl extends AbstractEffectiveDocumentedNodeWithoutStatus<String, ImportStatement>
        implements ImportEffectiveStatement, ModuleImport {

    private final String moduleName;
    private final Revision revision;
    private final SemVer semVer;
    private final String prefix;

    ImportEffectiveStatementImpl(final StmtContext<String, ImportStatement, ?> ctx) {
        super(ctx);

        moduleName = ctx.coerceStatementArgument();
        final Optional<String> prefixStmt = findFirstEffectiveSubstatementArgument(PrefixEffectiveStatement.class);
        MissingSubstatementException.throwIf(!prefixStmt.isPresent(), ctx.getStatementSourceReference(),
            "Prefix is mandatory substatement of import statement");
        this.prefix = prefixStmt.get();

        if (!ctx.isEnabledSemanticVersioning()) {
            final Optional<Revision> optRev = findFirstEffectiveSubstatementArgument(
                RevisionDateEffectiveStatement.class);
            this.revision = optRev.isPresent() ? optRev.get() : getImportedRevision(ctx);
            this.semVer = null;
        } else {
            final SemVerSourceIdentifier importedModuleIdentifier = ctx.getFromNamespace(
                ImportPrefixToSemVerSourceIdentifier.class, prefix);
            revision = importedModuleIdentifier.getRevision().orElse(null);
            semVer = importedModuleIdentifier.getSemanticVersion().orElse(null);
        }
    }

    private Revision getImportedRevision(final StmtContext<String, ImportStatement, ?> ctx) {
        /*
         * When 'revision-date' of an import is not specified in yang source, we
         * need to find revision of imported module.
         */
        final QNameModule importedModule = StmtContextUtils.getModuleQNameByPrefix(ctx, this.prefix);
        SourceException.throwIfNull(importedModule, ctx.getStatementSourceReference(),
                "Unable to find import of module %s with prefix %s.", this.moduleName, this.prefix);
        return importedModule.getRevision().orElse(null);
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    public Optional<SemVer> getSemanticVersion() {
        return Optional.ofNullable(semVer);
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues().add("moduleName", getModuleName())
                .add("revision", revision).add("version", semVer).add("prefix", getPrefix())
                .add("description", nullableDescription()).add("reference", nullableReference()).toString();
    }
}
