/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for {@link GetFilterElementAttributesStatement} and its effective view.
 */
// FIXME: 15:0.0: hide this class
public final class GetFilterElementAttributesStatementSupport extends AbstractEmptyStatementSupport<
        GetFilterElementAttributesStatement, GetFilterElementAttributesEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(GetFilterElementAttributesStatementSupport.class);
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(GetFilterElementAttributesStatement.DEFINITION).build();

    public GetFilterElementAttributesStatementSupport(final YangParserConfiguration config) {
        super(GetFilterElementAttributesStatement.DEFINITION, StatementPolicy.reject(), config, VALIDATOR);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<Empty, GetFilterElementAttributesStatement,
            GetFilterElementAttributesEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        if (!computeSupported(stmt)) {
            stmt.setUnsupported();
        }
    }

    @Override
    protected GetFilterElementAttributesStatement createDeclared(final BoundStmtCtx<Empty> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? GetFilterElementAttributesStatementImpl.EMPTY
            : new GetFilterElementAttributesStatementImpl(substatements);
    }

    @Override
    protected GetFilterElementAttributesStatement attachDeclarationReference(
            final GetFilterElementAttributesStatement stmt, final DeclarationReference reference) {
        return new RefGetFilterElementAttributesStatement(stmt, reference);
    }

    @Override
    protected GetFilterElementAttributesEffectiveStatement createEffective(
            final Current<Empty, GetFilterElementAttributesStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new GetFilterElementAttributesEffectiveStatementImpl(stmt, substatements);
    }

    private static boolean computeSupported(final StmtContext<?, ?, ?> stmt) {
        final var parent = stmt.getParentContext();
        if (parent == null) {
            LOG.debug("No parent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (!parent.producesDeclared(AnyxmlStatement.class)) {
            LOG.debug("Parent is not an anyxml node, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (!"filter".equals(parent.rawArgument())) {
            LOG.debug("Parent is not named 'filter', ignoring get-filter-element-attributes statement");
            return false;
        }

        final var grandParent = parent.getParentContext();
        if (grandParent == null) {
            LOG.debug("No grandparent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (!grandParent.producesDeclared(InputStatement.class)) {
            LOG.debug("Grandparent is not an input node, ignoring get-filter-element-attributes statement");
            return false;
        }

        final var greatGrandParent = grandParent.getParentContext();
        if (greatGrandParent == null) {
            LOG.debug("No grandparent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (!greatGrandParent.producesDeclared(RpcStatement.class)) {
            LOG.debug("Grandparent is not an RPC node, ignoring get-filter-element-attributes statement");
            return false;
        }

        return switch (greatGrandParent.getRawArgument()) {
            case "get", "get-config" -> true;
            default -> {
                LOG.debug("Great-grandparent is not named 'get' nor 'get-config, ignoring get-filter-element-attributes"
                    + " statement");
                yield false;
            }
        };
    }
}
