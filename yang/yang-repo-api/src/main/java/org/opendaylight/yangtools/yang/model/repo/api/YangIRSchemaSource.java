/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceInfo.Import;

@Beta
public final class YangIRSchemaSource extends AbstractSimpleIdentifiable<SourceIdentifier>
        implements YangSchemaSourceRepresentation {
    private static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    private static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final String MODULE = YangStmtMapping.MODULE.getStatementName().getLocalName();
    private static final String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();
    private static final String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    private static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final String SUBMODULE = YangStmtMapping.SUBMODULE.getStatementName().getLocalName();
    private static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();

    private final @NonNull IRStatement rootStatement;
    private final @Nullable String symbolicName;

    public YangIRSchemaSource(final @NonNull SourceIdentifier identifier, final @NonNull IRStatement rootStatement,
            final @Nullable String symbolicName) {
        super(identifier);
        this.rootStatement = requireNonNull(rootStatement);
        this.symbolicName = symbolicName;

        final var rootKeyword = rootStatement.keyword();
        checkArgument(rootKeyword instanceof IRKeyword.Unqualified,
                "Root statement has invalid keyword %s", rootKeyword);
        final var rootName = rootKeyword.identifier();
        switch (rootName) {
            case "module":
            case "submodule":
                break;
            default:
                throw new IllegalArgumentException("Invalid root statement keyword " + rootName);
        }

        checkArgument(rootStatement.argument() != null, "Root statement does not have an argument");
    }

    public static @NonNull YangIRSchemaSource fromSource(final SourceIdentifier sourceId,
            final @NonNull IRStatement rootStatement, final @Nullable String symbolicName) {
        return new YangIRSchemaSource(new SourceIdentifier(
            safeStringArgument(sourceId, rootStatement, "name"),
            latestRevision(rootStatement, sourceId)), rootStatement, symbolicName);
    }

    @Override
    public Optional<String> getSymbolicName() {
        return Optional.ofNullable(symbolicName);
    }

    @Override
    public Class<YangIRSchemaSource> getType() {
        return YangIRSchemaSource.class;
    }

    /**
     * Return the root statement of this source.
     *
     * @return Root statement.
     */
    public @NonNull IRStatement getRootStatement() {
        return rootStatement;
    }

    public @NonNull SchemaSourceInfo extractInfo() {
        final var keyword = rootStatement.keyword();
        checkArgument(keyword instanceof IRKeyword.Unqualified, "Invalid root statement %s", keyword);

        final var arg = keyword.identifier();
        if (MODULE.equals(arg)) {
            return new ModuleSourceInfo(Unqualified.of(safeStringArgument(rootStatement, "module name")),
                extractYangVersion(), extractNamespace(), prefixIn(rootStatement), extractRevisions(), extractImports(),
                extractIncludes());
        } else if (SUBMODULE.equals(arg)) {
            return new SubmoduleSourceInfo(Unqualified.of(safeStringArgument(rootStatement, "submodule name")),
                extractYangVersion(), extractBelongsTo(), extractRevisions(), extractImports(), extractIncludes());
        } else {
            throw new IllegalArgumentException("Root of parsed AST must be either module or submodule");
        }
    }

    private @NonNull YangVersion extractYangVersion() {
        return rootStatement.statements().stream()
            .filter(stmt -> isStatement(stmt, YANG_VERSION))
            .findFirst()
            .map(stmt -> YangVersion.ofString(safeStringArgument(stmt, "yang-version version")))
            .orElse(YangVersion.VERSION_1);
    }

    private @NonNull XMLNamespace extractNamespace() {
        return rootStatement.statements().stream()
            .filter(stmt -> isStatement(stmt, NAMESPACE))
            .findFirst()
            .map(stmt -> XMLNamespace.of(safeStringArgument(stmt, "namespace string")))
            .orElseThrow(() -> new IllegalArgumentException("Missing namespace in " + getIdentifier()));
    }

    private @NonNull Unqualified extractBelongsTo() {
        return rootStatement.statements().stream()
            .filter(stmt -> isStatement(stmt, BELONGS_TO))
            .findFirst()
            .map(stmt -> Unqualified.of(safeStringArgument(stmt, "belongs-to module name")))
            .orElseThrow(() -> new IllegalArgumentException("Missing belongs-to in " + getIdentifier()));
    }

    private @NonNull ImmutableSet<SchemaSourceInfo.Import> extractImports() {
        return rootStatement.statements().stream()
            .filter(stmt -> isStatement(stmt, IMPORT))
            .map(stmt -> new Import(
                Unqualified.of(safeStringArgument(stmt, "imported module name")),
                prefixIn(stmt),
                revisionDateIn(stmt)))
            .collect(ImmutableSet.toImmutableSet());
    }

    private @NonNull ImmutableSet<SchemaSourceInfo.Include> extractIncludes() {
        return rootStatement.statements().stream()
            .filter(stmt -> isStatement(stmt, INCLUDE))
            .map(stmt -> new SchemaSourceInfo.Include(
                Unqualified.of(safeStringArgument(stmt, "included submodule name")),
                revisionDateIn(stmt)))
            .collect(ImmutableSet.toImmutableSet());
    }

    private @NonNull ImmutableList<Revision> extractRevisions() {
        return rootStatement.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION))
            .map(stmt -> Revision.of(safeStringArgument(stmt, "revision date")))
            .collect(ImmutableList.toImmutableList());
    }

    private @NonNull String prefixIn(final IRStatement parent) {
        return parent.statements().stream()
            .filter(stmt -> isStatement(stmt, PREFIX))
            .findFirst()
            .map(stmt -> safeStringArgument(stmt, "prefix string"))
            .orElseThrow(() -> new IllegalArgumentException("No prefix in " + parent));
    }

    private @Nullable Revision revisionDateIn(final IRStatement parent) {
        return parent.statements().stream()
            .filter(stmt -> isStatement(stmt, REVISION_DATE))
            .findFirst()
            .map(stmt -> Revision.of(safeStringArgument(stmt, "imported module revision-date")))
            .orElse(null);
    }

    private static boolean isStatement(final IRStatement stmt, final String localName) {
        return stmt.keyword() instanceof IRKeyword.Unqualified keyword && localName.equals(keyword.identifier());
    }

    private String safeStringArgument(final IRStatement stmt, final String desc) {
        return safeStringArgument(getIdentifier(), stmt, desc);
    }

    // SourceIdentifier, Ref, whatever works here
    private static String safeStringArgument(final SourceIdentifier sourceId, final IRStatement stmt,
            final String desc) {
        final var arg = stmt.argument();
        if (arg == null) {
            throw new IllegalArgumentException("Missing " + desc + " at " + sourceId);
        }

        // TODO: we probably need to understand yang version first....
        return ArgumentContextUtils.rfc6020().stringFromStringContext(arg, sourceId);
    }
}
