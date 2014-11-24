/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import com.google.common.annotations.Beta;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Beta
public class SingleModuleYangStatementWriter implements StatementWriter {

    final PrintWriter writer;
    final URI currentModuleNs;
    final BiMap<String, URI> prefixToNamespace;

    final int indentSize = 4;
    int indentLevel = 0;


    private SingleModuleYangStatementWriter(final PrintWriter writer, final URI moduleNamespace,
            final Map<String, URI> prefixToNs) {
        super();
        this.writer = writer;
        this.currentModuleNs = moduleNamespace;
        this.prefixToNamespace = HashBiMap.create(prefixToNs);
    }

    static final StatementWriter create(final PrintWriter writer, final URI moduleNs, final Map<String,URI> prefixToNs) {
        return new SingleModuleYangStatementWriter(writer, moduleNs, prefixToNs);
    }

    @Override
    public void startStatement(final StatementDefinition statement) {
        writeStatement0(statement);
        // TODO Auto-generated method stub

    }



    @Override
    public void writeArgument(final RevisionAwareXPath xpath) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeArgument(final QName name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeArgument(final String argStr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeArgument(final SchemaPath targetPath) {
        // TODO Auto-generated method stub

    }

    @Override
    public void endStatement() {
        // TODO Auto-generated method stub

    }

    private void writeStatement0(final StatementDefinition statement) {
        writer.print(statement.getIdentifier());

    }

    private void writeQName(final QName name) {

    }

}
