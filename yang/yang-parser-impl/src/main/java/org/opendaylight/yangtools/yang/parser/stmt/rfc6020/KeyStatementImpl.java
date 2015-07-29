/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.KeyEffectiveStatementImpl;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import com.google.common.base.Splitter;

public class KeyStatementImpl extends AbstractDeclaredStatement<Collection<SchemaNodeIdentifier>> implements
        KeyStatement {

    protected KeyStatementImpl(StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Collection<SchemaNodeIdentifier>, KeyStatement,
                    EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> {

        public Definition() {
            super(Rfc6020Mapping.KEY);
        }

        @Override
        public Collection<SchemaNodeIdentifier> parseArgumentValue(StmtContext<?, ?, ?> ctx, String value)
                throws SourceException {

            Splitter keySplitter = Splitter.on(StmtContextUtils.LIST_KEY_SEPARATOR).omitEmptyStrings().trimResults();
            List<String> keyTokens = keySplitter.splitToList(value);

            // to detect if key contains duplicates
            if ((new HashSet<>(keyTokens)).size() < keyTokens.size()) {
                throw new SourceException(
                        String.format("Duplicate value in list key: %s\n", value), ctx.getStatementSourceReference());
            }

            Set<SchemaNodeIdentifier> keyNodes = new HashSet<>();

            for (String keyToken : keyTokens) {

                SchemaNodeIdentifier keyNode = SchemaNodeIdentifier
                        .create(false, Utils.qNameFromArgument(ctx, keyToken));
                keyNodes.add(keyNode);
            }

            return keyNodes;
        }

        @Override
        public KeyStatement createDeclared(StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> ctx) {
            return new KeyStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement> createEffective(
                StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement,
                        EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> ctx) {
            return new KeyEffectiveStatementImpl(ctx);
        }
    }

}
