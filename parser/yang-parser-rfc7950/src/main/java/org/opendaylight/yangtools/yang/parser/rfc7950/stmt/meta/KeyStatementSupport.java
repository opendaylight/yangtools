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
import java.util.LinkedHashSet;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementLexer;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class KeyStatementSupport
        extends AbstractStatementSupport<KeyArgument, KeyStatement, KeyEffectiveStatement> {
    /**
     * This is equivalent to {@link YangStatementLexer#SEP}'s definition. Currently equivalent to the non-repeating
     * part of:
     *
     * <p>{@code SEP: [ \n\r\t]+ -> type(SEP);}.
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
        SubstatementValidator.builder(KeyStatement.DEFINITION).build();

    public KeyStatementSupport(final YangParserConfiguration config) {
        super(KeyStatement.DEFINITION, StatementPolicy.copyDeclared(
            // Identity comparison is sufficient because adaptArgumentValue() is careful about reuse.
            (copy, current, substatements) -> copy.getArgument() == current.getArgument()),
            config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public KeyArgument parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final var nodeIdentifiers = new LinkedHashSet<QName>();
        final var binding = ctx.identifierBinding();
        for (var keyToken : KEY_ARG_SPLITTER.split(value)) {
            if (!nodeIdentifiers.add(binding.parseNodeIdentifierArg(ctx, keyToken))) {
                throw new SourceException(ctx, "Key argument '%s' contains duplicates", value);
            }
        }
        return KeyArgument.of(List.copyOf(nodeIdentifiers));
    }

    @Override
    public KeyArgument adaptArgumentValue(final StmtContext<KeyArgument, KeyStatement, KeyEffectiveStatement> ctx,
            final QNameModule targetModule) {
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
