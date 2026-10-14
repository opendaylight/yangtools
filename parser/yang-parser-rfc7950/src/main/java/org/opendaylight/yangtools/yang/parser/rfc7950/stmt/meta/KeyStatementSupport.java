/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.CommonStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.IdentifierBinding;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceAction;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.InferenceContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder.Prerequisite;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class KeyStatementSupport
        extends AbstractStatementSupport<KeyArgument, KeyStatement, KeyEffectiveStatement> {
    /**
     * This is equivalent to {@code YangStatementLexer.g4}'s {@code SEP} definition. Currently equivalent to the
     * non-repeating part of {@code SEP: [ \n\r\t]+ -> type(SEP);}.
     */
    private static final CharMatcher SEP = CharMatcher.anyOf(" \n\r\t").precomputed();

    /**
     * Splitter corresponding to {@code key-arg}'s ABNF as defined
     * in <a href="https://www.rfc-editor.org/rfc/rfc6020#section-12">RFC6020, section 12</a>.
     *
     * <p>{@code key-arg             = node-identifier *(sep node-identifier)}
     *
     * <p>We also account for {@link #SEP} not handling repetition by ignoring empty strings.
     */
    private static final Splitter KEY_ARG_SPLITTER = Splitter.on(SEP).omitEmptyStrings();

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(KeyStatement.DEF).build();

    public KeyStatementSupport(final YangParserConfiguration config) {
        super(KeyStatement.DEF, StatementPolicy.copyDeclared(
            // Identity comparison is sufficient because adaptArgumentValue() is careful about reuse.
            (copy, current, substatements) -> copy.getArgument() == current.getArgument()),
            config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<KeyArgument, KeyStatement, KeyEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        final var list = stmt.coerceParentContext().asDeclaring(ListStatement.DEF);
        if (list == null) {
            return;
        }

        for (var qname : stmt.getArgument().asList()) {
            final var action = stmt.newInferenceAction(ModelProcessingPhase.EFFECTIVE_MODEL);
            final var req = action.requiresCtx(list, ParserNamespaces.schemaTree(), qname,
                ModelProcessingPhase.EFFECTIVE_MODEL);
            action.apply(new InferenceAction() {
                @Override
                public void apply(final InferenceContext ctx) {
                    final var child = req.resolve(ctx);
                    final var leaf = child.asDeclaring(LeafStatement.DEF);
                    if (leaf == null) {
                        throw new InferenceException(list, "Key '%s' component '%s' refers to a child %s",
                            stmt.getRawArgument(), qname.getLocalName(), child.publicDefinition().humanName());
                    }

                    // RFC7950 forbids use of 'if-feature' and 'when' on list keys
                    if (list.sourceVersion() != YangVersion.VERSION_1) {
                        StmtContextUtils.inferUnconditionalLeaf(list, leaf);
                    }
                }

                @Override
                public void prerequisiteFailed(final Collection<? extends Prerequisite<?>> failed) {
                    throw new InferenceException(list, "Key '%s' misses node '%s' in list '%s'",
                        stmt.getRawArgument(), qname.getLocalName(), list.argument());
                }
            });
        }
    }

    @Override
    public KeyArgument parseArgumentValue(final CommonStmtCtx stmt, final IdentifierBinding binding,
            final String rawArgument) {
        final var nodeIdentifiers = new LinkedHashSet<QName>();
        for (var keyToken : KEY_ARG_SPLITTER.split(rawArgument)) {
            if (!nodeIdentifiers.add(binding.parseNodeIdentifierArg(stmt, keyToken))) {
                throw new SourceException(stmt, "Key argument '%s' contains duplicates", rawArgument);
            }
        }
        return KeyArgument.of(List.copyOf(nodeIdentifiers));
    }

    @Override
    public KeyArgument adaptArgumentValue(final StmtContext<KeyArgument, ?, ?> ctx, final QNameModule targetModule) {
        final var original = ctx.getArgument();
        final var adapted = new ArrayList<QName>(original.size());
        boolean canReuse = true;
        for (var qname : original) {
            if (!targetModule.equals(qname.getModule())) {
                final var newQname = qname.bindTo(targetModule).intern();
                adapted.add(newQname);
                canReuse = false;
            } else {
                adapted.add(qname);
            }
        }

        // This makes sure we reuse the collection when a grouping is instantiated in the same module.
        return canReuse ? original : KeyArgument.of(adapted);
    }

    @Override
    protected KeyStatement createDeclared(final BoundStmtCtx<KeyArgument> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createKey(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected KeyStatement attachDeclarationReference(final KeyStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateKey(stmt, reference);
    }

    @Override
    protected KeyEffectiveStatement createEffective(final Current<KeyArgument, KeyStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createKey(stmt.declared(), stmt.getArgument(), substatements);
    }
}
