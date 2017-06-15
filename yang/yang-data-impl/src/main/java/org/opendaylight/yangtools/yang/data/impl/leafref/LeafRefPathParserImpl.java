/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.io.IOException;
import java.io.InputStream;
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

     public LeafRefPathParserImpl(final SchemaContext schemaContext, final Module currentModule, final SchemaNode currentNode) {
        this.schemaContext = schemaContext;
        this.module = currentModule;
        this.node = currentNode;
    }

    public LeafRefPath parseLeafRefPathSourceToSchemaPath(final InputStream stream) throws IOException, LeafRefYangSyntaxErrorException {

        final Path_argContext pathCtx = parseLeafRefPathSource(stream);

        final ParseTreeWalker walker = new ParseTreeWalker();
        final LeafRefPathParserListenerImpl leafRefPathParserListenerImpl = new LeafRefPathParserListenerImpl(schemaContext, module, node);
        walker.walk(leafRefPathParserListenerImpl,pathCtx);

        final LeafRefPath leafRefPath = leafRefPathParserListenerImpl.getLeafRefPath();

        return leafRefPath;
    }


    private Path_argContext parseLeafRefPathSource(final InputStream stream) throws IOException, LeafRefYangSyntaxErrorException {
        final LeafRefPathLexer lexer = new LeafRefPathLexer(CharStreams.fromStream(stream));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final LeafRefPathParser parser = new LeafRefPathParser(tokens);
        parser.removeErrorListeners();

        final LeafRefPathErrorListener errorListener = new LeafRefPathErrorListener(module);
        parser.addErrorListener(errorListener);

        final Path_argContext result = parser.path_arg();
        errorListener.validate();

        return result;
    }

}
