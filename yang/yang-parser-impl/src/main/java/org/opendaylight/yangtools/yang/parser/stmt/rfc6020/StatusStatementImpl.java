/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;

public class StatusStatementImpl extends AbstractDeclaredStatement<Status>
        implements StatusStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .STATUS)
            .build();

    protected StatusStatementImpl(
            final StmtContext<Status, StatusStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Status, StatusStatement, EffectiveStatement<Status, StatusStatement>> {

        public Definition() {
            super(YangStmtMapping.STATUS);
        }

        @Override
        public Status parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            switch (value) {
                case "current":
                    return Status.CURRENT;
                case "deprecated":
                    return Status.DEPRECATED;
                case "obsolete":
                    return Status.OBSOLETE;
                default:
                    throw new SourceException(ctx.getStatementSourceReference(),
                        "Invalid status '%s', must be one of 'current', 'deprecated' or 'obsolete'", value);
            }
        }

        @Override
        public StatusStatement createDeclared(
                final StmtContext<Status, StatusStatement, ?> ctx) {
            return new StatusStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Status, StatusStatement> createEffective(
                final StmtContext<Status, StatusStatement, EffectiveStatement<Status, StatusStatement>> ctx) {
            return new StatusEffectiveStatementImpl(ctx);
        }

        @Override
        public String internArgument(final String rawArgument) {
            if ("current".equals(rawArgument)) {
                return "current";
            } else if ("deprecated".equals(rawArgument)) {
                return "deprecated";
            } else if ("obsolete".equals(rawArgument)) {
                return "obsolete";
            } else {
                return rawArgument;
            }
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public Status getValue() {
        return argument();
    }

}
