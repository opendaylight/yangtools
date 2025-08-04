/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.module;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ResolvedSourceInfo;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

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
    protected ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final Current<Unqualified, ModuleStatement> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> substatements) {
        final var local = super.buildEffectiveSubstatements(stmt, substatements);
        final var submodules = submoduleContexts(stmt);
        if (submodules.isEmpty()) {
            return local;
        }

        // Concatenate statements so they appear as if they were part of target module
        final var others = new ArrayList<EffectiveStatement<?, ?>>();
        for (var submoduleCtx : submodules) {
            for (var effective : submoduleCtx.buildEffective().effectiveSubstatements()) {
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

        final var submodules = new ArrayList<Submodule>();
        for (var submoduleCtx : submoduleContexts(stmt)) {
            final var submodule = submoduleCtx.buildEffective();
            if (!(submodule instanceof Submodule legacy)) {
                throw new VerifyException("Submodule statement " + submodule + " is not a Submodule");
            }
            submodules.add(legacy);
        }

        final var resolve = verifyNotNull(stmt.namespaceItem(ParserNamespaces.RESOLVED_INFO, Empty.value()));
        try {
            return new ModuleEffectiveStatementImpl(stmt, substatements, submodules, resolve.qnameModule());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static Collection<StmtContext<?, ?, ?>> submoduleContexts(final Current<?, ?> stmt) {
        final var resolvedInfo = stmt.namespaceItem(ParserNamespaces.RESOLVED_INFO, Empty.value());
        return resolvedInfo == null ? List.of() : resolvedInfo.includes().stream()
            .map(ResolvedSourceInfo.ResolvedInclude::rootContext).collect(Collectors.toSet());
    }

    private static SourceException noNamespace(final @NonNull CommonStmtCtx stmt) {
        return new SourceException("No namespace declared in module", stmt);
    }
}
