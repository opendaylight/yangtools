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
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrganizationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
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
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ModuleStatementSupport
        extends AbstractUnqualifiedStatementSupport<ModuleStatement, ModuleEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(ModuleStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(AugmentStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addOptional(ContactStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(DeviationStatement.DEF)
            .addAny(ExtensionStatement.DEF)
            .addAny(FeatureStatement.DEF)
            .addAny(GroupingStatement.DEF)
            .addAny(IdentityStatement.DEF)
            .addAny(ImportStatement.DEF)
            .addAny(IncludeStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addMandatory(NamespaceStatement.DEF)
            .addAny(NotificationStatement.DEF)
            .addOptional(OrganizationStatement.DEF)
            .addMandatory(PrefixStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addAny(RevisionStatement.DEF)
            .addAny(RpcStatement.DEF)
            .addAny(TypedefStatement.DEF)
            .addAny(UsesStatement.DEF)
            .addOptional(YangVersionStatement.DEF)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(ModuleStatement.DEF)
            .addAny(AnydataStatement.DEF)
            .addAny(AnyxmlStatement.DEF)
            .addAny(AugmentStatement.DEF)
            .addAny(ChoiceStatement.DEF)
            .addOptional(ContactStatement.DEF)
            .addAny(ContainerStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addAny(DeviationStatement.DEF)
            .addAny(ExtensionStatement.DEF)
            .addAny(FeatureStatement.DEF)
            .addAny(GroupingStatement.DEF)
            .addAny(IdentityStatement.DEF)
            .addAny(ImportStatement.DEF)
            .addAny(IncludeStatement.DEF)
            .addAny(LeafStatement.DEF)
            .addAny(LeafListStatement.DEF)
            .addAny(ListStatement.DEF)
            .addMandatory(NamespaceStatement.DEF)
            .addAny(NotificationStatement.DEF)
            .addOptional(OrganizationStatement.DEF)
            .addMandatory(PrefixStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addAny(RevisionStatement.DEF)
            .addAny(RpcStatement.DEF)
            .addAny(TypedefStatement.DEF)
            .addAny(UsesStatement.DEF)
            .addMandatory(YangVersionStatement.DEF)
            .build();

    private ModuleStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(ModuleStatement.DEF, StatementPolicy.reject(), config, validator);
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

        final var qnameModule = verifyNotNull(stmt.namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME,
            (StmtContext<?,?,?>)stmt));
        try {
            return new ModuleEffectiveStatementImpl(stmt, substatements, submodules, qnameModule);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static Collection<StmtContext<?, ?, ?>> submoduleContexts(final Current<?, ?> stmt) {
        final var submodules = stmt.localNamespacePortion(ParserNamespaces.INCLUDED_SUBMODULE_NAME_TO_MODULECTX);
        return submodules == null ? List.of() : submodules.values();
    }

    private static SourceException noNamespace(final @NonNull CommonStmtCtx stmt) {
        return new SourceException("No namespace declared in module", stmt);
    }
}
