/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameCacheNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.KeyEffectiveStatementImpl;

public class KeyStatementImpl extends AbstractDeclaredStatement<Collection<SchemaNodeIdentifier>> implements
        KeyStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .KEY)
            .build();

    protected KeyStatementImpl(final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Collection<SchemaNodeIdentifier>, KeyStatement,
                    EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> {

        public Definition() {
            super(YangStmtMapping.KEY);
        }

        @Override
        public Collection<SchemaNodeIdentifier> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            final Builder<SchemaNodeIdentifier> builder = ImmutableSet.builder();
            int tokens = 0;
            for (String keyToken : StmtContextUtils.LIST_KEY_SPLITTER.split(value)) {
                builder.add(SchemaNodeIdentifier.create(false, StmtContextUtils.qnameFromArgument(ctx, keyToken)));
                tokens++;
            }

            // Throws NPE on nulls, retains first inserted value, cannot be modified
            final Collection<SchemaNodeIdentifier> ret = builder.build();
            SourceException.throwIf(ret.size() != tokens, ctx.getStatementSourceReference(),
                    "Key argument '%s' contains duplicates", value);
            return ret;
        }

        @Override
        public Collection<SchemaNodeIdentifier> adaptArgumentValue(
                final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement,
                    EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> ctx,
                final QNameModule targetModule) {
            final Builder<SchemaNodeIdentifier> builder = ImmutableSet.builder();
            boolean replaced = false;
            for (final SchemaNodeIdentifier arg : ctx.getStatementArgument()) {
                final QName qname = arg.getLastComponent();
                if (!targetModule.equals(qname)) {
                    final QName newQname = ctx.getFromNamespace(QNameCacheNamespace.class,
                            QName.create(targetModule, qname.getLocalName()));
                    builder.add(SchemaNodeIdentifier.create(false, newQname));
                    replaced = true;
                } else {
                    builder.add(arg);
                }
            }

            // This makes sure we reuse the collection when a grouping is
            // instantiated in the same module
            return replaced ? builder.build() : ctx.getStatementArgument();
        }

        @Override
        public KeyStatement createDeclared(final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> ctx) {
            return new KeyStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement> createEffective(
                final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement,
                        EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> ctx) {
            return new KeyEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }
}
