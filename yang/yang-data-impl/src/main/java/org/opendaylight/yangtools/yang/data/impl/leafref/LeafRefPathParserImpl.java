/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.yang.data.impl.leafref.LeafRefPathParser.Path_argContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

final class LeafRefPathParserImpl {
    private final SchemaContext schemaContext;
    private final Module module;
    private final SchemaNode node;

    LeafRefPathParserImpl(final SchemaContext schemaContext, final Module currentModule, final SchemaNode currentNode) {
        this.schemaContext = schemaContext;
        this.module = currentModule;
        this.node = currentNode;
    }

    LeafRefPath parseLeafRefPath(final String path) throws LeafRefYangSyntaxErrorException {
        final Path_argContext pathCtx = parseLeafRefPathSource(path);

        final ParseTreeWalker walker = new ParseTreeWalker();
        final LeafRefPathParserListenerImpl leafRefPathParserListenerImpl = new LeafRefPathParserListenerImpl(
            schemaContext, module, node);
        walker.walk(leafRefPathParserListenerImpl, pathCtx);

        return leafRefPathParserListenerImpl.getLeafRefPath();
    }

    private Path_argContext parseLeafRefPathSource(final String path) throws LeafRefYangSyntaxErrorException {
        final LeafRefPathLexer lexer = new LeafRefPathLexer(CharStreams.fromString(path));
        final LeafRefPathParser parser = new LeafRefPathParser(new CommonTokenStream(lexer));

        final LeafRefPathErrorListener errorListener = new LeafRefPathErrorListener(module);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        final Path_argContext result = parser.path_arg();
        errorListener.validate();
        return result;
    }
}
