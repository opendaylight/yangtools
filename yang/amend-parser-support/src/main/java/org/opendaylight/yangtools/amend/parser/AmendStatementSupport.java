/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.amend.parser;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.yangtools.amend.model.api.AmendStatements.AMEND;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.parseIdentifier;
import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.parseNodeIdentifier;

import com.google.common.base.Splitter;
import com.google.common.primitives.UnsignedInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.amend.model.api.AmendEffectiveStatement;
import org.opendaylight.yangtools.amend.model.api.AmendStatement;
import org.opendaylight.yangtools.amend.model.api.StatementPath;
import org.opendaylight.yangtools.amend.model.api.StatementPath.ModuleIdentifier;
import org.opendaylight.yangtools.amend.model.api.StatementPath.StatementIdentifier;
import org.opendaylight.yangtools.amend.model.api.StatementPath.StatementNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
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

public final class AmendStatementSupport
        extends AbstractStatementSupport<StatementPath, AmendStatement, AmendEffectiveStatement> {
    private static final AmendStatementSupport INSTANCE = new AmendStatementSupport(AMEND);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    private final SubstatementValidator validator;

    private AmendStatementSupport(final StatementDefinition definition) {
        super(definition);
        validator = SubstatementValidator.builder(definition).build();
    }

    public static AmendStatementSupport getInstance() {
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
    public AmendStatement createDeclared(final StmtContext<StatementPath, AmendStatement, ?> ctx) {
        return new AmendStatementImpl(ctx);
    }

    @Override
    public AmendEffectiveStatement createEffective(
            final StmtContext<StatementPath, AmendStatement, AmendEffectiveStatement> ctx) {
        return new AmendEffectiveStatementImpl(ctx);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<StatementPath, AmendStatement, AmendEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final List<StatementIdentifier<?>> targetPath = stmt.getStatementArgument().getElements();
        final Iterator<StatementIdentifier<?>> it = targetPath.iterator();
        final StatementIdentifier<?> first = it.next();
        verify(first instanceof ModuleIdentifier, "Invalid first statement %s", first);
        final String prefix = ((ModuleIdentifier)first).getValue();

        final ModelActionBuilder action = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
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

    void applyAmend(final Mutable<?, ?, EffectiveStatement<?, ?>> target,
            final Mutable<StatementPath, AmendStatement, AmendEffectiveStatement> stmt,
            final Iterator<StatementIdentifier<?>> it) {
        // TODO Auto-generated method stub

    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}
