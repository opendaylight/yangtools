/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.repo.api.SemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToSemVerSourceIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class ImportEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, ImportStatement>
        implements ModuleImport {

    private final String moduleName;
    private final Revision revision;
    private final SemVer semVer;
    private final String prefix;
    private final String description;
    private final String reference;

    public ImportEffectiveStatementImpl(final StmtContext<String, ImportStatement, ?> ctx) {
        super(ctx);

        moduleName = ctx.getStatementArgument();
        final PrefixEffectiveStatementImpl prefixStmt = firstEffective(PrefixEffectiveStatementImpl.class);
        if (prefixStmt != null) {
            this.prefix = prefixStmt.argument();
        } else {
            throw new MissingSubstatementException("Prefix is mandatory substatement of import statement",
                    ctx.getStatementSourceReference());
        }

        if (!ctx.isEnabledSemanticVersioning()) {
            final RevisionDateEffectiveStatementImpl revisionDateStmt = firstEffective(
                RevisionDateEffectiveStatementImpl.class);
            this.revision = revisionDateStmt == null ? getImportedRevision(ctx) : revisionDateStmt.argument();
            this.semVer = null;
        } else {
            final SemVerSourceIdentifier importedModuleIdentifier = ctx.getFromNamespace(
                ImportPrefixToSemVerSourceIdentifier.class, prefix);
            revision = Revision.valueOf(importedModuleIdentifier.getRevision());
            semVer = importedModuleIdentifier.getSemanticVersion().orElse(null);
        }

        final DescriptionEffectiveStatementImpl descriptionStmt = firstEffective(
            DescriptionEffectiveStatementImpl.class);
        this.description = descriptionStmt != null ? descriptionStmt.argument() : null;

        final ReferenceEffectiveStatementImpl referenceStmt = firstEffective(ReferenceEffectiveStatementImpl.class);
        this.reference = referenceStmt != null ? referenceStmt.argument() : null;
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
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleName, revision, prefix, semVer, description, reference);
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
        final ImportEffectiveStatementImpl other = (ImportEffectiveStatementImpl) obj;
        return Objects.equals(moduleName, other.moduleName) && Objects.equals(revision, other.revision)
                && Objects.equals(semVer, other.semVer) && Objects.equals(prefix, other.prefix)
                && Objects.equals(description, other.description) && Objects.equals(reference, other.reference);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("moduleName", getModuleName())
                .add("revision", getRevision()).add("semantic version", getSemanticVersion())
                .add("prefix", getPrefix()).add("description", getDescription())
                .add("reference", getReference()).toString();
    }
}
