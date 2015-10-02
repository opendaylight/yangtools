/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.KeyEffectiveStatementImpl;

public class KeyStatementImpl extends AbstractDeclaredStatement<Collection<SchemaNodeIdentifier>> implements
        KeyStatement {

    protected KeyStatementImpl(final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<Collection<SchemaNodeIdentifier>, KeyStatement, EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> {

        public Definition() {
            super(Rfc6020Mapping.KEY);
        }

        @Override
        public Collection<SchemaNodeIdentifier> parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value)
                throws SourceException {

            final List<String> keyTokens = StmtContextUtils.LIST_KEY_SPLITTER.splitToList(value);

            // to detect if key contains duplicates
            if ((new HashSet<>(keyTokens)).size() < keyTokens.size()) {
                throw new IllegalArgumentException();
            }

            // FIXME: would an ImmutableSetBuilder be better?
            Set<SchemaNodeIdentifier> keyNodes = new LinkedHashSet<>();
            for (String keyToken : keyTokens) {
                keyNodes.add(SchemaNodeIdentifier.create(false, Utils.qNameFromArgument(ctx, keyToken)));
            }

            return keyNodes;
        }

        @Override
        public KeyStatement createDeclared(final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> ctx) {
            return new KeyStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement> createEffective(
                final StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> ctx) {
            return new KeyEffectiveStatementImpl(ctx);
        }
    }

}
