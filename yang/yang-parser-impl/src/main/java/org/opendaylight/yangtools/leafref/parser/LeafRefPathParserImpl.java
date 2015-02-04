/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafref.parser;

import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;

import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser.Path_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathLexer;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;


public final class LeafRefPathParserImpl {
    private SchemaContext schemaContext;
    private Module module;
    private SchemaNode node;

     public LeafRefPathParserImpl(SchemaContext schemaContext, Module currentModule, SchemaNode currentNode) {
        this.schemaContext = schemaContext;
        this.module = currentModule;
        this.node = currentNode;
    }

    public LeafRefPath parseLeafRefPathSourceToSchemaPath(InputStream stream) throws IOException, YangSyntaxErrorException {

        Path_argContext pathCtx = parseLeafRefPathSource(stream);

        final ParseTreeWalker walker = new ParseTreeWalker();
        LeafRefPathParserListenerImpl leafRefPathParserListenerImpl = new LeafRefPathParserListenerImpl(schemaContext, module, node);
        walker.walk(leafRefPathParserListenerImpl,pathCtx);

        LeafRefPath leafRefPath = leafRefPathParserListenerImpl.getLeafRefPath();

        return leafRefPath;
    }


    private Path_argContext parseLeafRefPathSource(final InputStream stream) throws IOException, YangSyntaxErrorException {
        final LeafRefPathLexer lexer = new LeafRefPathLexer(new ANTLRInputStream(stream));
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final LeafRefPathParser parser = new LeafRefPathParser(tokens);
        parser.removeErrorListeners();

        final LeafRefPathErrorListener errorListener = new LeafRefPathErrorListener(module);
        parser.addErrorListener(errorListener);

        Path_argContext result = parser.path_arg();
        errorListener.validate();

        return result;
    }

}
