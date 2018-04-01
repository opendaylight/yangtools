/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.append.parser;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.yangtools.append.model.api.AppendStatements.APPEND;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.parseIdentifier;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.parseNodeIdentifier;

import com.google.common.base.Splitter;
import com.google.common.primitives.UnsignedInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.append.model.api.AppendEffectiveStatement;
import org.opendaylight.yangtools.append.model.api.AppendStatement;
import org.opendaylight.yangtools.append.model.api.StatementPath;
import org.opendaylight.yangtools.append.model.api.StatementPath.ModuleIdentifier;
import org.opendaylight.yangtools.append.model.api.StatementPath.StatementIdentifier;
import org.opendaylight.yangtools.append.model.api.StatementPath.StatementNamespace;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.FeatureNamespace;
import org.opendaylight.yangtools.yang.parser.spi.GroupingNamespace;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.ImportPrefixToModuleCtx;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class AppendStatementSupport
        extends AbstractStatementSupport<StatementPath, AppendStatement, AppendEffectiveStatement> {
    private static final AppendStatementSupport INSTANCE = new AppendStatementSupport(APPEND);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    private AppendStatementSupport(final StatementDefinition definition) {
        super(definition);
    }

    public static AppendStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public StatementPath parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final List<String> components = SLASH_SPLITTER.splitToList(value);
        final List<StatementIdentifier<?>> elements = new ArrayList<>(components.size());

        try {
            StatementIdentifier<?> lastElement = null;
            for (String component : components) {
                final int hash = component.indexOf('#');
                final StatementNamespace namespace;
                if (hash != -1) {
                    final String prefix = component.substring(0, hash);
                    final Optional<StatementNamespace> optNamespace = StatementNamespace.forPrefix(prefix);
                    SourceException.throwIf(!optNamespace.isPresent(), ctx.getStatementSourceReference(),
                        "Invalid statement namespace \"%s\"", prefix);
                    namespace = optNamespace.get();
                    component = component.substring(hash + 1);
                } else {
                    SourceException.throwIf(lastElement == null, ctx.getStatementSourceReference(),
                            "Missing initial element prefix in \"%s\"", component);
                    namespace = lastElement.getNamespace();
                }

                switch (namespace) {
                    case CASE:
                        lastElement = StatementPath.caseIdentifier(parseNodeIdentifier(ctx, component));
                        break;
                    case DATA:
                        lastElement = StatementPath.dataIdentifier(parseNodeIdentifier(ctx, component));
                        break;
                    case EXTENSION:
                        lastElement = StatementPath.extensionIdentifier(parseNodeIdentifier(ctx, component));
                        break;
                    case FEATURE:
                        lastElement = StatementPath.featureIdentifier(parseNodeIdentifier(ctx, component));
                        break;
                    case GROUPING:
                        lastElement = StatementPath.groupingIdentifier(parseNodeIdentifier(ctx, component));
                        break;
                    case IDENTITY:
                        lastElement = StatementPath.identityIdentifier(parseNodeIdentifier(ctx, component));
                        break;
                    case MODULE:
                        lastElement = StatementPath.moduleIdentifier(parseIdentifier(ctx, component).getLocalName());
                        break;
                    case TYPEDEF:
                        lastElement = StatementPath.typedefIdentifier(parseNodeIdentifier(ctx, component));
                        break;
                    case UNION_MEMBER:
                        lastElement = StatementPath.unionMemberIdentifier(UnsignedInteger.valueOf(component, 10));
                        break;
                    default:
                        throw new InferenceException(ctx.getStatementSourceReference(),
                            "Unknown namespace %s encountered in argument \"%s\"", namespace, value);
                }

                elements.add(lastElement);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid argument string \"%s\"", value);
        }

        if (elements.isEmpty()) {
            throw new SourceException("Statement path may not be empty", ctx.getStatementSourceReference());
        }
        final StatementIdentifier<?> first = elements.get(0);
        SourceException.throwIf(!(first instanceof ModuleIdentifier), ctx.getStatementSourceReference(),
                "Statement path %s does not start with a module", elements);
        return StatementPath.of(elements);
    }

    @Override
    public AppendStatement createDeclared(final StmtContext<StatementPath, AppendStatement, ?> ctx) {
        return new AppendStatementImpl(ctx);
    }

    @Override
    public AppendEffectiveStatement createEffective(
            final StmtContext<StatementPath, AppendStatement, AppendEffectiveStatement> ctx) {
        return new AppendEffectiveStatementImpl(ctx);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<StatementPath, AppendStatement, AppendEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final List<StatementIdentifier<?>> targetPath = stmt.getStatementArgument().getElements();
        final Iterator<StatementIdentifier<?>> it = targetPath.iterator();
        final StatementIdentifier<?> first = it.next();
        verify(first instanceof ModuleIdentifier, "Invalid first statement %s", first);
        final String prefix = ((ModuleIdentifier)first).getValue();

        final StmtContext<?, ?, ?> targetModule = InferenceException.throwIfNull(
            stmt.getFromNamespace(ImportPrefixToModuleCtx.class, prefix),
            stmt.getStatementSourceReference(), "Failed to lookup module for %s", first);

        final ModelActionBuilder action = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
        final Prerequisite<? extends StmtContext<?, ?, ?>> declaredModule =
                action.requiresCtx(targetModule, ModelProcessingPhase.FULL_DECLARATION);
        final Prerequisite<Mutable<?, ?, EffectiveStatement<?, ?>>> modulePrereq = action.mutatesEffectiveCtx(stmt,
            ImportPrefixToModuleCtx.class, prefix);

        action.apply(new InferenceAction() {
            @Override
            public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                throw new InferenceException(stmt.getStatementSourceReference(),
                    "Failed to resolve prefix %s to a module", prefix);
            }

            @Override
            public void apply(final InferenceContext ctx) {
                applyAmend(modulePrereq.resolve(ctx), stmt, it);
            }
        });
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return null;
    }

    static void applyAmend(final Mutable<?, ?, EffectiveStatement<?, ?>> module,
            final StmtContext<StatementPath, AppendStatement, AppendEffectiveStatement> stmt,
            final Iterator<StatementIdentifier<?>> it) {

        StmtContext<?, ?, ?> target = module;
        while (it.hasNext()) {
            final StatementIdentifier<?> id = it.next();
            target = InferenceException.throwIfNull(findTarget(target, id),
                stmt.getStatementSourceReference(),
                "Failed to resolve statement path %s: child %s does not exist under %s", stmt.getStatementArgument(),
                id, target);

        }
    }

    private static StmtContext<?, ?, ?> findTarget(final StmtContext<?, ?, ?> target, final StatementIdentifier<?> id) {
        switch (id.getNamespace()) {
            case CASE:
                // FIXME: implement this
                return null;
            case DATA:
                return target.getFromNamespace(ChildSchemaNodeNamespace.class, (QName)id.getValue());
            case EXTENSION:
                return target.getFromNamespace(ExtensionNamespace.class, (QName)id.getValue());
            case FEATURE:
                return target.getFromNamespace(FeatureNamespace.class, (QName)id.getValue());
            case GROUPING:
                return target.getFromNamespace(GroupingNamespace.class, (QName)id.getValue());
            case IDENTITY:
                return target.getFromNamespace(GroupingNamespace.class, (QName)id.getValue());
            case MODULE:
                // FIXME: implement this
                return null;
            case TYPEDEF:
                return target.getFromNamespace(TypeNamespace.class, (QName)id.getValue());
            case UNION_MEMBER:
                // FIXME: implement this
                return null;
            default:
                throw new IllegalStateException("Unhandled namespace " + id.getNamespace());
        }
    }
}
