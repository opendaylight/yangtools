/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.io.IOException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import org.opendaylight.yangtools.leafref.parser.LeafRefPathParserImpl;
import org.opendaylight.yangtools.antlrv4.code.gen.LeafRefPathParser;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.Leafref;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class LeafRefContextTreeBuilder {
    private SchemaContext schemaContext;

    public LeafRefContextTreeBuilder(SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    public LeafRefContext buildLeafRefContextTree() throws IOException, YangSyntaxErrorException {
        LeafRefContext root = new LeafRefContextImpl(schemaContext.getQName(),
                schemaContext.getPath(), schemaContext, null);

        Set<Module> modules = schemaContext.getModules();
        for (Module module : modules) {
            Collection<DataSchemaNode> childNodes = module.getChildNodes();
            for (DataSchemaNode childNode : childNodes) {
                LeafRefContext childLeafRefContext = buildLeafRefContextTreeFor(
                        root, childNode, module);

                if (childLeafRefContext.hasReferencedByChild()
                        || childLeafRefContext.isReferencedBy()) {
                    root.addReferencedByChild(childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    root.addReferencingChild(childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }
        }

        return root;
    }

    private LeafRefContext buildLeafRefContextTreeFor(LeafRefContext parent,
            DataSchemaNode node, Module currentModule) throws IOException, YangSyntaxErrorException {

        LeafRefContext currentLeafRefContext = new LeafRefContextImpl(
                node.getQName(), node.getPath(), schemaContext, parent);

        if (node instanceof DataNodeContainer) {
            DataNodeContainer dataNodeContainer = (DataNodeContainer) node;
            Collection<DataSchemaNode> childNodes = dataNodeContainer
                    .getChildNodes();

            for (DataSchemaNode childNode : childNodes) {
                LeafRefContext childLeafRefContext = buildLeafRefContextTreeFor(
                        currentLeafRefContext, childNode,currentModule);

                if (childLeafRefContext.hasReferencedByChild()
                        || childLeafRefContext.isReferencedBy()) {
                    currentLeafRefContext.addReferencedByChild(
                            childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    currentLeafRefContext.addReferencingChild(
                            childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }
        } else if (node instanceof LeafSchemaNode) {
            LeafSchemaNode leaf = (LeafSchemaNode) node;
            TypeDefinition<?> type = leaf.getType();
            if (type instanceof Leafref) {
                Leafref leafrefType = (Leafref) type;
                String leafRefPathString = leafrefType.getPathStatement().toString();

                currentLeafRefContext.setLeafRefTargetPathString(leafRefPathString);
                currentLeafRefContext.setReferencing(true);

                LeafRefPathParserImpl leafRefPathParser = new LeafRefPathParserImpl(schemaContext,currentModule,leaf);

                ByteArrayInputStream leafRefPathInputStream = new ByteArrayInputStream(leafRefPathString.getBytes(Charset.forName("UTF-8")));
                LeafRefPath leafRefPath = leafRefPathParser.parseLeafRefPathSourceToSchemaPath(leafRefPathInputStream);
                currentLeafRefContext.setLeafRefTargetPath(leafRefPath);

                // :TODO check isReferencedBy too ...
            }
        } // :TODO else if instanceof leaflist ... etc. too

        return currentLeafRefContext;
    }

    // private LeafRefContext buildLeafRefContextTreeFor(LeafRefContext parent,
    // Module module) {
    //
    // Collection<DataSchemaNode> childNodes = module.getChildNodes();
    // for (DataSchemaNode childNode : childNodes) {
    // LeafRefContext childLeafRefContext = buildLeafRefContextTreeFor(parent,
    // childNode);
    //
    // if(childLeafRefContext.hasReferencedByChild() ||
    // childLeafRefContext.isReferencedBy()) {
    // parent.addReferencedByChild(childLeafRefContext,
    // childLeafRefContext.getCurrentNodeQName());
    // }
    // if(childLeafRefContext.hasReferencingChild() ||
    // childLeafRefContext.isReferencing()) {
    // parent.addReferencingChild(childLeafRefContext,
    // childLeafRefContext.getCurrentNodeQName());
    // }
    // }
    //
    // return node;
    // }

}
