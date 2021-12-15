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
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class UniqueStatementSupport
        extends AbstractStatementSupport<Set<Descendant>, UniqueStatement, UniqueEffectiveStatement> {
    /**
     * Support 'sep' ABNF rule in RFC7950 section 14. CRLF pattern is used to squash line-break from CRLF to LF form
     * and then we use SEP_SPLITTER, which can operate on single characters.
     */
    private static final Pattern CRLF_PATTERN = Pattern.compile("\r\n", Pattern.LITERAL);
    private static final Splitter SEP_SPLITTER = Splitter.on(CharMatcher.anyOf(" \t\n").precomputed())
            .omitEmptyStrings();

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.UNIQUE).build();

    public UniqueStatementSupport(final YangParserConfiguration config) {
        // FIXME: This reflects what the current implementation does. We really want to define an adaptArgumentValue(),
        //        but how that plays with the argument and expectations needs to be investigated.
        super(YangStmtMapping.UNIQUE, StatementPolicy.contextIndependent(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public ImmutableSet<Descendant> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final ImmutableSet<Descendant> uniqueConstraints = parseUniqueConstraintArgument(ctx, value);
        SourceException.throwIf(uniqueConstraints.isEmpty(), ctx,
            "Invalid argument value '%s' of unique statement. The value must contains at least one descendant schema "
                + "node identifier.", value);
        return uniqueConstraints;
    }

    @Override
    protected UniqueStatement createDeclared(final BoundStmtCtx<Set<Descendant>> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createUnique(ctx.getRawArgument(), ctx.getArgument(), substatements);
    }

    @Override
    protected UniqueStatement attachDeclarationReference(final UniqueStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateUnique(stmt, reference);
    }

    @Override
    protected UniqueEffectiveStatement createEffective(final Current<Set<Descendant>, UniqueStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createUnique(stmt.declared(), substatements);
    }

    private static ImmutableSet<Descendant> parseUniqueConstraintArgument(final StmtContext<?, ?, ?> ctx,
            final String argumentValue) {
        // deal with 'line-break' rule, which is either "\n" or "\r\n", but not "\r"
        final String nocrlf = CRLF_PATTERN.matcher(argumentValue).replaceAll("\n");

        final Set<Descendant> uniqueConstraintNodes = new HashSet<>();
        for (final String uniqueArgToken : SEP_SPLITTER.split(nocrlf)) {
            final SchemaNodeIdentifier nodeIdentifier = ArgumentUtils.nodeIdentifierFromPath(ctx, uniqueArgToken);
            SourceException.throwIf(nodeIdentifier instanceof Absolute, ctx,
                "Unique statement argument '%s' contains schema node identifier '%s' which is not in the descendant "
                    + "node identifier form.", argumentValue, uniqueArgToken);
            uniqueConstraintNodes.add((Descendant) nodeIdentifier);
        }
        return ImmutableSet.copyOf(uniqueConstraintNodes);
    }
}
