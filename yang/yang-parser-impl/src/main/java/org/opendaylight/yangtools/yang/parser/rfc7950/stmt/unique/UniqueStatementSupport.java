/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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

public final class UniqueStatementSupport
        extends AbstractStatementSupport<Collection<SchemaNodeIdentifier.Relative>, UniqueStatement,
                EffectiveStatement<Collection<SchemaNodeIdentifier.Relative>, UniqueStatement>> {
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.UNIQUE)
        .build();

    public UniqueStatementSupport() {
        super(YangStmtMapping.UNIQUE);
    }

    @Override
    public Collection<SchemaNodeIdentifier.Relative> parseArgumentValue(final StmtContext<?, ?, ?> ctx,
            final String value) {
        final Collection<Relative> uniqueConstraints = parseUniqueConstraintArgument(ctx, value);
        SourceException.throwIf(uniqueConstraints.isEmpty(), ctx.getStatementSourceReference(),
                "Invalid argument value '%s' of unique statement. The value must contains at least "
                        + "one descendant schema node identifier.", value);
        return uniqueConstraints;
    }

    @Override
    public UniqueStatement createDeclared(final StmtContext<Collection<Relative>, UniqueStatement, ?> ctx) {
        return new UniqueStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<Collection<Relative>, UniqueStatement> createEffective(
            final StmtContext<Collection<Relative>, UniqueStatement,
            EffectiveStatement<Collection<Relative>, UniqueStatement>> ctx) {
        return new UniqueEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    private static Collection<SchemaNodeIdentifier.Relative> parseUniqueConstraintArgument(
            final StmtContext<?, ?, ?> ctx, final String argumentValue) {
        final Set<SchemaNodeIdentifier.Relative> uniqueConstraintNodes = new HashSet<>();
        for (final String uniqueArgToken : SPACE_SPLITTER.split(argumentValue)) {
            final SchemaNodeIdentifier nodeIdentifier = ArgumentUtils.nodeIdentifierFromPath(ctx, uniqueArgToken);
            SourceException.throwIf(nodeIdentifier.isAbsolute(), ctx.getStatementSourceReference(),
                    "Unique statement argument '%s' contains schema node identifier '%s' "
                            + "which is not in the descendant node identifier form.", argumentValue, uniqueArgToken);
            uniqueConstraintNodes.add((SchemaNodeIdentifier.Relative) nodeIdentifier);
        }
        return ImmutableSet.copyOf(uniqueConstraintNodes);
    }
}