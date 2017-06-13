/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.MissingSubstatementException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToSemVerModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StmtContextUtils;

public class ImportEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, ImportStatement> implements
        ModuleImport {

    private final String moduleName;
    private final Date revision;
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
            final RevisionDateEffectiveStatementImpl revisionDateStmt = firstEffective(RevisionDateEffectiveStatementImpl.class);
            this.revision = (revisionDateStmt == null) ? getImportedRevision(ctx) : revisionDateStmt
                    .argument();
            this.semVer = Module.DEFAULT_SEMANTIC_VERSION;
        } else {
            final ModuleIdentifier importedModuleIdentifier = ctx.getFromNamespace(ImpPrefixToSemVerModuleIdentifier.class, prefix);
            revision = importedModuleIdentifier.getRevision();
            semVer = importedModuleIdentifier.getSemanticVersion();
        }

        final DescriptionEffectiveStatementImpl descriptionStmt = firstEffective(DescriptionEffectiveStatementImpl.class);
        this.description = (descriptionStmt != null) ? descriptionStmt.argument() : null;

        final ReferenceEffectiveStatementImpl referenceStmt = firstEffective(ReferenceEffectiveStatementImpl.class);
        this.reference = (referenceStmt != null) ? referenceStmt.argument() : null;
    }

    private Date getImportedRevision(final StmtContext<String, ImportStatement, ?> ctx) {
        /*
         * When 'revision-date' of an import is not specified in yang source, we
         * need to find revision of imported module.
         */
        final QNameModule importedModule = StmtContextUtils.getModuleQNameByPrefix(ctx, this.prefix);
        SourceException.throwIfNull(importedModule, ctx.getStatementSourceReference(),
                "Unable to find import of module %s with prefix %s.", this.moduleName, this.prefix);
        return importedModule.getRevision();
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
