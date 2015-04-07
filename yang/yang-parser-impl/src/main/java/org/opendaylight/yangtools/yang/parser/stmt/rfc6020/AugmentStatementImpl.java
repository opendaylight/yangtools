/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import com.google.common.base.Splitter;

public class AugmentStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier> implements AugmentStatement {

    protected AugmentStatementImpl(StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> {

        public static final char SEPARATOR = '/';

        public Definition() {
            super(Rfc6020Mapping.Augment);
        }

        @Override
        public SchemaNodeIdentifier parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {

            String regexPathAbs = "/(.+)";
            String regexPathRel = "\\.\\.\\s*/(.+)";

            String path;
            boolean absPath;

            if (value.matches(regexPathAbs)) {
                absPath = true;
                path = value;
            } else if (value.matches(regexPathRel)) {
                absPath = false;

                Pattern pattern = Pattern.compile(regexPathRel);
                Matcher matcher = pattern.matcher(value);

                matcher.matches();
                path = matcher.group(1);
            } else {
                throw new IllegalArgumentException();
            }

            Splitter keySplitter = Splitter.on(SEPARATOR).omitEmptyStrings().trimResults();
            List<String> nodeNames = keySplitter.splitToList(path);

            ArrayList<QName> qNames = new ArrayList<>();

            for (String nodeName : nodeNames) {

                try {
                    final QName qName = Utils.qNameFromArgument(ctx, nodeName);
                    qNames.add(qName);
                } catch (Exception e) {
                    throw new IllegalArgumentException();
                }
            }

            return SchemaNodeIdentifier.create(qNames, absPath);
        }

        @Override
        public AugmentStatement createDeclared(StmtContext<SchemaNodeIdentifier, AugmentStatement, ?> ctx) {
            return new AugmentStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<SchemaNodeIdentifier, AugmentStatement> createEffective(
                StmtContext<SchemaNodeIdentifier, AugmentStatement, EffectiveStatement<SchemaNodeIdentifier, AugmentStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull
    @Override
    public SchemaNodeIdentifier getTargetNode() {
        return argument();
    }

    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }
}
