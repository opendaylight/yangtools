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
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesEffectiveStatement;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesSchemaNode;
import org.opendaylight.yangtools.rfc6241.model.api.GetFilterElementAttributesStatement;
import org.opendaylight.yangtools.rfc6241.model.api.NetconfStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithoutArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class GetFilterElementAttributesStatementSupport extends AbstractEmptyStatementSupport<
        GetFilterElementAttributesStatement, GetFilterElementAttributesEffectiveStatement> {

    private static final class Declared extends WithSubstatements implements GetFilterElementAttributesStatement {
        static final @NonNull Declared EMPTY = new Declared(ImmutableList.of());

        Declared(final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(substatements);
        }
    }

    private static final class Effective
            extends UnknownEffectiveStatementBase<Empty, GetFilterElementAttributesStatement>
            implements GetFilterElementAttributesEffectiveStatement, GetFilterElementAttributesSchemaNode {
        private final @NonNull Immutable path;

        Effective(final Current<Empty, GetFilterElementAttributesStatement> stmt,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(stmt, substatements);
            path = SchemaPathSupport.toEffectivePath(stmt.getEffectiveParent().getSchemaPath()
                    .createChild(stmt.publicDefinition().getStatementName()));
        }

        @Override
        public QName getQName() {
            return SchemaNodeDefaults.extractQName(path);
        }

        @Override
        @Deprecated
        public SchemaPath getPath() {
            return SchemaNodeDefaults.extractPath(this, path);
        }

        @Override
        public GetFilterElementAttributesEffectiveStatement asEffectiveStatement() {
            return this;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(GetFilterElementAttributesStatementSupport.class);
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(NetconfStatements.GET_FILTER_ELEMENT_ATTRIBUTES).build();

    public GetFilterElementAttributesStatementSupport(final YangParserConfiguration config) {
        super(NetconfStatements.GET_FILTER_ELEMENT_ATTRIBUTES, StatementPolicy.reject(), config);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<Empty, GetFilterElementAttributesStatement,
            GetFilterElementAttributesEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);
        stmt.setIsSupportedToBuildEffective(computeSupported(stmt));
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }

    @Override
    protected GetFilterElementAttributesStatement createDeclared(
            final StmtContext<Empty, GetFilterElementAttributesStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return substatements.isEmpty() ? Declared.EMPTY : new Declared(substatements);
    }

    @Override
    protected GetFilterElementAttributesEffectiveStatement createEffective(
            final Current<Empty, GetFilterElementAttributesStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(stmt, substatements);
    }

    private static boolean computeSupported(final StmtContext<?, ?, ?> stmt) {
        final StmtContext<?, ?, ?> parent = stmt.getParentContext();
        if (parent == null) {
            LOG.debug("No parent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (parent.publicDefinition() != YangStmtMapping.ANYXML) {
            LOG.debug("Parent is not an anyxml node, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (!"filter".equals(parent.rawArgument())) {
            LOG.debug("Parent is not named 'filter', ignoring get-filter-element-attributes statement");
            return false;
        }

        final StmtContext<?, ?, ?> grandParent = parent.getParentContext();
        if (grandParent == null) {
            LOG.debug("No grandparent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (grandParent.publicDefinition() != YangStmtMapping.INPUT) {
            LOG.debug("Grandparent is not an input node, ignoring get-filter-element-attributes statement");
            return false;
        }

        final StmtContext<?, ?, ?> greatGrandParent = grandParent.getParentContext();
        if (greatGrandParent == null) {
            LOG.debug("No grandparent, ignoring get-filter-element-attributes statement");
            return false;
        }
        if (greatGrandParent.publicDefinition() != YangStmtMapping.RPC) {
            LOG.debug("Grandparent is not an RPC node, ignoring get-filter-element-attributes statement");
            return false;
        }

        switch (greatGrandParent.getRawArgument()) {
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
