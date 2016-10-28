/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc6020.repo;

import com.google.common.base.Preconditions;
import javax.xml.transform.TransformerException;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YinDomSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinXmlSchemaSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.w3c.dom.Node;

public final class YinStatementStreamSource implements Identifiable<SourceIdentifier>, StatementStreamSource {
    private final SourceIdentifier identifier;
    private final Node root;

    private YinStatementStreamSource(final SourceIdentifier identifier, final Node root) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.root = Preconditions.checkNotNull(root);
    }

    public static StatementStreamSource create(final YinXmlSchemaSource source) throws TransformerException {
        return create(YinDomSchemaSource.transform(source));
    }

    public static StatementStreamSource create(final YinDomSchemaSource source) {
        return new YinStatementStreamSource(source.getIdentifier(), source.getSource().getNode());
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        // TODO Auto-generated method stub

    }
}
