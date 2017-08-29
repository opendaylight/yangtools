/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.InvalidLengthConstraintException;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.model.util.type.StringTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringTypeEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement> implements
        TypeEffectiveStatement<TypeStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(StringTypeEffectiveStatementImpl.class);
    private final StringTypeDefinition typeDefinition;

    public StringTypeEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final StringTypeDefinition baseType) {
        super(ctx);

        final StringTypeBuilder builder = RestrictedTypes.newStringBuilder(baseType,
            TypeUtils.typeEffectiveSchemaPath(ctx));

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof LengthEffectiveStatementImpl) {
                final LengthEffectiveStatementImpl length = (LengthEffectiveStatementImpl)stmt;

                try {
                    builder.setLengthConstraint(length, length.argument());
                } catch (IllegalStateException e) {
                    throw new SourceException(ctx.getStatementSourceReference(), e,
                            "Multiple length constraints encountered");
                } catch (InvalidLengthConstraintException e) {
                    throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid length constraint %s",
                        length.argument());
                }
            }
            if (stmt instanceof PatternEffectiveStatementImpl) {
                final PatternConstraint pattern = ((PatternEffectiveStatementImpl)stmt).argument();
                if (pattern != null) {
                    builder.addPatternConstraint(pattern);
                } else {
                    LOG.debug("Ignoring empty pattern statement {}", stmt);
                }
            }
            if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Nonnull
    @Override
    public StringTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
