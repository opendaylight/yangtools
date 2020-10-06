/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.rfc6241.model.api.NetconfStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseVoidStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class GetFilterElementAttributesStatementSupport extends BaseVoidStatementSupport<
        GetFilterElementAttributesStatement, GetFilterElementAttributesEffectiveStatement> {

    private static final class Declared extends WithSubstatements implements GetFilterElementAttributesStatement {
        static final @NonNull Declared EMPTY = new Declared(ImmutableList.of());

        Declared(final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(substatements);
        }
    }

    private static final class Effective
            extends UnknownEffectiveStatementBase<Void, GetFilterElementAttributesStatement>
            implements GetFilterElementAttributesEffectiveStatement, GetFilterElementAttributesSchemaNode {
        private final @NonNull SchemaPath path;

        Effective(final GetFilterElementAttributesStatement declared,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
                final StmtContext<Void, GetFilterElementAttributesStatement, ?> ctx) {
            super(declared.argument(), declared, substatements, ctx);
            path = ctx.coerceParentContext().getSchemaPath().get().createChild(
                ctx.getPublicDefinition().getStatementName());
        }

        @Override
        public QName getQName() {
            return path.getLastComponent();
        }

        @Override
        @Deprecated
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public GetFilterElementAttributesEffectiveStatement asEffectiveStatement() {
            return this;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(GetFilterElementAttributesStatementSupport.class);
    private static final GetFilterElementAttributesStatementSupport INSTANCE =
            new GetFilterElementAttributesStatementSupport(NetconfStatements.GET_FILTER_ELEMENT_ATTRIBUTES);

    private final SubstatementValidator validator;

    GetFilterElementAttributesStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition).build();
    }

    public static GetFilterElementAttributesStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<Void, GetFilterElementAttributesStatement,
            GetFilterElementAttributesEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.setIsSupportedToBuildEffective(computeSupported(stmt));
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected GetFilterElementAttributesStatement createDeclared(
            final StmtContext<Void, GetFilterElementAttributesStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(substatements);
    }

    @Override
    protected GetFilterElementAttributesStatement createEmptyDeclared(
            final StmtContext<Void, GetFilterElementAttributesStatement, ?> ctx) {
        return Declared.EMPTY;
    }

    @Override
    protected GetFilterElementAttributesEffectiveStatement createEffective(
            final StmtContext<Void, GetFilterElementAttributesStatement,
                GetFilterElementAttributesEffectiveStatement> ctx,
            final GetFilterElementAttributesStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(declared, substatements, ctx);
    }

    @Override
    protected GetFilterElementAttributesEffectiveStatement createEmptyEffective(
            final StmtContext<Void, GetFilterElementAttributesStatement,
                GetFilterElementAttributesEffectiveStatement> ctx,
            final GetFilterElementAttributesStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }

    private static boolean computeSupported(final StmtContext<?, ?, ?> stmt) {
        final StmtContext<?, ?, ?> parent = stmt.getParentContext();
        if (parent == null) {
            LOG.debug("No parent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (parent.getPublicDefinition() != YangStmtMapping.ANYXML) {
            LOG.debug("Parent is not an anyxml node, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (!"filter".equals(parent.rawStatementArgument())) {
            LOG.debug("Parent is not named 'filter', ignoring get-filter-element-attributes statement");
            return false;
        }

        final StmtContext<?, ?, ?> grandParent = parent.getParentContext();
        if (grandParent == null) {
            LOG.debug("No grandparent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (grandParent.getPublicDefinition() != YangStmtMapping.INPUT) {
            LOG.debug("Grandparent is not an input node, ignoring get-filter-element-attributes statement");
            return false;
        }

        final StmtContext<?, ?, ?> greatGrandParent = grandParent.getParentContext();
        if (greatGrandParent == null) {
            LOG.debug("No grandparent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (greatGrandParent.getPublicDefinition() != YangStmtMapping.RPC) {
            LOG.debug("Grandparent is not an RPC node, ignoring get-filter-element-attributes statement");
            return false;
        }

        switch (greatGrandParent.rawStatementArgument()) {
            case "get":
            case "get-config":
                return true;
            default:
                LOG.debug("Great-grandparent is not named 'get' nor 'get-config, ignoring get-filter-element-attributes"
                    + " statement");
                return false;
        }
    }
}
