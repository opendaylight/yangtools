/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class UniqueStatementSupport extends AbstractStatementSupport<Set<Relative>, UniqueStatement,
        EffectiveStatement<Set<Relative>, UniqueStatement>> {
    /**
     * Support 'sep' ABNF rule in RFC7950 section 14. CRLF pattern is used to squash line-break from CRLF to LF form
     * and then we use SEP_SPLITTER, which can operate on single characters.
     */
    private static final Pattern CRLF_PATTERN = Pattern.compile("\r\n", Pattern.LITERAL);
    private static final Splitter SEP_SPLITTER = Splitter.on(CharMatcher.anyOf(" \t\n").precomputed())
            .omitEmptyStrings();

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.UNIQUE)
        .build();
    private static final UniqueStatementSupport INSTANCE = new UniqueStatementSupport();

    private UniqueStatementSupport() {
        super(YangStmtMapping.UNIQUE);
    }

    public static UniqueStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Set<Relative> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        final Set<Relative> uniqueConstraints = parseUniqueConstraintArgument(ctx, value);
        SourceException.throwIf(uniqueConstraints.isEmpty(), ctx.getStatementSourceReference(),
                "Invalid argument value '%s' of unique statement. The value must contains at least "
                        + "one descendant schema node identifier.", value);
        return uniqueConstraints;
    }

    @Override
    public UniqueStatement createDeclared(final StmtContext<Set<Relative>, UniqueStatement, ?> ctx) {
        return new UniqueStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<Set<Relative>, UniqueStatement> createEffective(
            final StmtContext<Set<Relative>, UniqueStatement, EffectiveStatement<Set<Relative>, UniqueStatement>> ctx) {
        return new UniqueEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    private static Set<Relative> parseUniqueConstraintArgument(final StmtContext<?, ?, ?> ctx,
            final String argumentValue) {
        // deal with 'line-break' rule, which is either "\n" or "\r\n", but not "\r"
        final String nocrlf = CRLF_PATTERN.matcher(argumentValue).replaceAll("\n");

        final Set<Relative> uniqueConstraintNodes = new HashSet<>();
        for (final String uniqueArgToken : SEP_SPLITTER.split(nocrlf)) {
            final SchemaNodeIdentifier nodeIdentifier = ArgumentUtils.nodeIdentifierFromPath(ctx, uniqueArgToken);
            SourceException.throwIf(nodeIdentifier.isAbsolute(), ctx.getStatementSourceReference(),
                    "Unique statement argument '%s' contains schema node identifier '%s' "
                            + "which is not in the descendant node identifier form.", argumentValue, uniqueArgToken);
            uniqueConstraintNodes.add((Relative) nodeIdentifier);
        }
        return ImmutableSet.copyOf(uniqueConstraintNodes);
    }
}