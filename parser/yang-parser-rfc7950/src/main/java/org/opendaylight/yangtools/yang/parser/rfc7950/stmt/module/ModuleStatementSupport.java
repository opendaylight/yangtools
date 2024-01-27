/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class ModuleStatementSupport
        extends AbstractUnqualifiedStatementSupport<ModuleStatement, ModuleEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.MODULE)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.AUGMENT)
        .addAny(YangStmtMapping.CHOICE)
        .addOptional(YangStmtMapping.CONTACT)
        .addAny(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.DEVIATION)
        .addAny(YangStmtMapping.EXTENSION)
        .addAny(YangStmtMapping.FEATURE)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IDENTITY)
        .addAny(YangStmtMapping.IMPORT)
        .addAny(YangStmtMapping.INCLUDE)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addMandatory(YangStmtMapping.NAMESPACE)
        .addAny(YangStmtMapping.NOTIFICATION)
        .addOptional(YangStmtMapping.ORGANIZATION)
        .addMandatory(YangStmtMapping.PREFIX)
        .addOptional(YangStmtMapping.REFERENCE)
        .addAny(YangStmtMapping.REVISION)
        .addAny(YangStmtMapping.RPC)
        .addAny(YangStmtMapping.TYPEDEF)
        .addAny(YangStmtMapping.USES)
        .addOptional(YangStmtMapping.YANG_VERSION)
        .build();
    private static final SubstatementValidator RFC7950_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.MODULE)
        .addAny(YangStmtMapping.ANYDATA)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.AUGMENT)
        .addAny(YangStmtMapping.CHOICE)
        .addOptional(YangStmtMapping.CONTACT)
        .addAny(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.DEVIATION)
        .addAny(YangStmtMapping.EXTENSION)
        .addAny(YangStmtMapping.FEATURE)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IDENTITY)
        .addAny(YangStmtMapping.IMPORT)
        .addAny(YangStmtMapping.INCLUDE)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addMandatory(YangStmtMapping.NAMESPACE)
        .addAny(YangStmtMapping.NOTIFICATION)
        .addOptional(YangStmtMapping.ORGANIZATION)
        .addMandatory(YangStmtMapping.PREFIX)
        .addOptional(YangStmtMapping.REFERENCE)
        .addAny(YangStmtMapping.REVISION)
        .addAny(YangStmtMapping.RPC)
        .addAny(YangStmtMapping.TYPEDEF)
        .addAny(YangStmtMapping.USES)
        .addMandatory(YangStmtMapping.YANG_VERSION)
        .build();

    private ModuleStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.MODULE, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull ModuleStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new ModuleStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull ModuleStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new ModuleStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public void onPreLinkageDeclared(final Mutable<Unqualified, ModuleStatement, ModuleEffectiveStatement> stmt) {
        final Unqualified moduleName = stmt.getArgument();

        final XMLNamespace moduleNs = SourceException.throwIfNull(
            firstAttributeOf(stmt.declaredSubstatements(), NamespaceStatement.class), stmt,
            "Namespace of the module [%s] is missing", moduleName);
        stmt.addToNs(ParserNamespaces.MODULE_NAME_TO_NAMESPACE, moduleName, moduleNs);

        final String modulePrefix = SourceException.throwIfNull(
            firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class), stmt,
            "Prefix of the module [%s] is missing", moduleName);
        stmt.addToNs(ParserNamespaces.IMP_PREFIX_TO_NAMESPACE, modulePrefix, moduleNs);

        stmt.addToNs(ParserNamespaces.PRELINKAGE_MODULE, moduleName, stmt);

        final Revision revisionDate = StmtContextUtils.getLatestRevision(stmt.declaredSubstatements()).orElse(null);
        final QNameModule qNameModule = QNameModule.ofRevision(moduleNs, revisionDate).intern();

        stmt.addToNs(ParserNamespaces.MODULECTX_TO_QNAME, stmt, qNameModule);
        stmt.setRootIdentifier(new SourceIdentifier(stmt.getArgument(), revisionDate));
    }

    @Override
    public void onLinkageDeclared(final Mutable<Unqualified, ModuleStatement, ModuleEffectiveStatement> stmt) {
        final XMLNamespace moduleNs = SourceException.throwIfNull(
            firstAttributeOf(stmt.declaredSubstatements(), NamespaceStatement.class), stmt,
            "Namespace of the module [%s] is missing", stmt.argument());

        final Revision revisionDate = StmtContextUtils.getLatestRevision(stmt.declaredSubstatements()).orElse(null);
        final QNameModule qNameModule = QNameModule.ofRevision(moduleNs, revisionDate).intern();
        final StmtContext<?, ModuleStatement, ModuleEffectiveStatement> possibleDuplicateModule =
                stmt.namespaceItem(ParserNamespaces.NAMESPACE_TO_MODULE, qNameModule);
        if (possibleDuplicateModule != null && possibleDuplicateModule != stmt) {
            throw new SourceException(stmt, "Module namespace collision: %s. At %s", qNameModule.namespace(),
                possibleDuplicateModule.sourceReference());
        }

        final Unqualified moduleName = stmt.getArgument();
        final SourceIdentifier moduleIdentifier = new SourceIdentifier(moduleName, revisionDate);

        stmt.addToNs(ParserNamespaces.MODULE, moduleIdentifier, stmt);
        stmt.addToNs(ParserNamespaces.MODULE_FOR_BELONGSTO, moduleName, stmt);
        stmt.addToNs(ParserNamespaces.NAMESPACE_TO_MODULE, qNameModule, stmt);

        final String modulePrefix = SourceException.throwIfNull(
            firstAttributeOf(stmt.declaredSubstatements(), PrefixStatement.class), stmt,
            "Prefix of the module [%s] is missing", stmt.argument());

        stmt.addToNs(QNameModuleNamespace.INSTANCE, Empty.value(), qNameModule);
        stmt.addToNs(ParserNamespaces.PREFIX_TO_MODULE, modulePrefix, qNameModule);
        stmt.addToNs(ParserNamespaces.MODULE_NAME_TO_QNAME, moduleName, qNameModule);
        stmt.addToNs(ParserNamespaces.MODULECTX_TO_QNAME, stmt, qNameModule);
        stmt.addToNs(ParserNamespaces.MODULECTX_TO_SOURCE, stmt, moduleIdentifier);
        stmt.addToNs(ParserNamespaces.MODULE_NAMESPACE_TO_NAME, qNameModule, moduleName);
        stmt.addToNs(ParserNamespaces.IMPORT_PREFIX_TO_MODULECTX, modulePrefix, stmt);
    }

    @Override
    protected ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final Current<Unqualified, ModuleStatement> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> substatements) {
        final ImmutableList<? extends EffectiveStatement<?, ?>> local =
                super.buildEffectiveSubstatements(stmt, substatements);
        final Collection<StmtContext<?, ?, ?>> submodules = submoduleContexts(stmt);
        if (submodules.isEmpty()) {
            return local;
        }

        // Concatenate statements so they appear as if they were part of target module
        final List<EffectiveStatement<?, ?>> others = new ArrayList<>();
        for (StmtContext<?, ?, ?> submoduleCtx : submodules) {
            for (EffectiveStatement<?, ?> effective : submoduleCtx.buildEffective().effectiveSubstatements()) {
                if (effective instanceof SchemaNode || effective instanceof DataNodeContainer) {
                    others.add(effective);
                }
            }
        }

        return ImmutableList.<EffectiveStatement<?, ?>>builderWithExpectedSize(local.size() + others.size())
                .addAll(local)
                .addAll(others)
                .build();
    }

    @Override
    protected ModuleStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        if (substatements.isEmpty()) {
            throw noNamespace(ctx);
        }
        return DeclaredStatements.createModule(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected ModuleStatement attachDeclarationReference(final ModuleStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateModule(stmt, reference);
    }

    @Override
    protected ModuleEffectiveStatement createEffective(final Current<Unqualified, ModuleStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        if (substatements.isEmpty()) {
            throw noNamespace(stmt);
        }

        final List<Submodule> submodules = new ArrayList<>();
        for (StmtContext<?, ?, ?> submoduleCtx : submoduleContexts(stmt)) {
            final EffectiveStatement<?, ?> submodule = submoduleCtx.buildEffective();
            verify(submodule instanceof Submodule, "Submodule statement %s is not a Submodule", submodule);
            submodules.add((Submodule) submodule);
        }

        final QNameModule qnameModule = verifyNotNull(stmt.namespaceItem(QNameModuleNamespace.INSTANCE, Empty.value()));
        try {
            return new ModuleEffectiveStatementImpl(stmt, substatements, submodules, qnameModule);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static Collection<StmtContext<?, ?, ?>> submoduleContexts(final Current<?, ?> stmt) {
        final Map<Unqualified, StmtContext<?, ?, ?>> submodules = stmt.localNamespacePortion(
            ParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX);
        return submodules == null ? List.of() : submodules.values();
    }

    private static SourceException noNamespace(final @NonNull CommonStmtCtx stmt) {
        return new SourceException("No namespace declared in module", stmt);
    }
}
