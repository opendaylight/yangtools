package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

public abstract sealed class SourceInfoExtractor<T> permits YinSourceInfoExtr, YangSourceInfoExtr {

    static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    static final String MODULE = YangStmtMapping.MODULE.getStatementName().getLocalName();
    static final String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();
    static final String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    static final String SUBMODULE = YangStmtMapping.SUBMODULE.getStatementName().getLocalName();
    static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();
    static final String CONTACT = YangStmtMapping.CONTACT.getStatementName().getLocalName();
    static final String ORGANIZATION = YangStmtMapping.ORGANIZATION.getStatementName().getLocalName();
    static final String DESCRIPTION = YangStmtMapping.DESCRIPTION.getStatementName().getLocalName();
    static final String REFERENCE = YangStmtMapping.REFERENCE.getStatementName().getLocalName();

    abstract String extractRootType(T root);
    abstract Referenced<Unqualified> extractPrefix(T root, SourceIdentifier rootId);
    abstract Referenced<XMLNamespace> extractNamespace(T root, SourceIdentifier rootId);
    abstract BelongsTo extractBelongsTo(T root, SourceIdentifier rootId);
    abstract Referenced<Unqualified> extractName(T root, SourceIdentifier rootId);
    abstract Referenced<YangVersion> extractYangVersion(T root, SourceIdentifier rootId);

    abstract void fillRevisions(SourceInfo.Builder<?, ?> builder, T root, SourceIdentifier rootId);
    abstract void fillIncludes(SourceInfo.Builder<?, ?> builder, T root, SourceIdentifier rootId);
    abstract void fillImports(SourceInfo.Builder<?, ?> builder, T root, SourceIdentifier rootId);

    //TODO: these might not be necessary if we move the creation of all Statements to the StatementDefinition phase.
    abstract Referenced<String> extractContact(T root, SourceIdentifier rootId);
    abstract Referenced<String> extractOrganization(T root, SourceIdentifier rootId);
    abstract Referenced<String> extractDescription(T root, SourceIdentifier rootId);
    abstract Referenced<String> extractReference(T root, SourceIdentifier rootId);

    public final @NonNull SourceInfo forRoot(T root, SourceIdentifier rootIdentifier) {
        final String rootType = extractRootType(root);
        if (rootType.equals(MODULE)) {
            return extractModule(root, rootIdentifier);
        }

        if (rootType.equals(SUBMODULE)) {
            return extractSubmodule(root, rootIdentifier);
        }
        throw new IllegalArgumentException("Root of YING must be either module or submodule");
    }

    private SourceInfo.@NonNull Module extractModule(T root, SourceIdentifier rootId) {
        final var builder = SourceInfo.Module.builder();
        fillCommon(builder, root, rootId);
        return builder
            .setPrefix(extractPrefix(root, rootId))
            .setNamespace(extractNamespace(root, rootId))
            .build();
    }

    private SourceInfo.@NonNull Submodule extractSubmodule(T root, SourceIdentifier rootId) {
        final var builder = SourceInfo.Submodule.builder();
        fillCommon(builder, root, rootId);
        return builder
            .setBelongsTo(extractBelongsTo(root, rootId))
            .build();
    }

    private void fillCommon(final SourceInfo.Builder<?, ?> builder, final T root,
        final SourceIdentifier rootId) {
        builder.setName(extractName(root, rootId))
            .setYangVersion(extractYangVersion(root, rootId))
            .setContact(extractContact(root, rootId))
            .setOrganization(extractOrganization(root, rootId))
            .setDescription(extractDescription(root, rootId))
            .setReference(extractReference(root, rootId));
        fillRevisions(builder, root, rootId);
        fillIncludes(builder, root, rootId);
        fillImports(builder, root, rootId);
    }
}
