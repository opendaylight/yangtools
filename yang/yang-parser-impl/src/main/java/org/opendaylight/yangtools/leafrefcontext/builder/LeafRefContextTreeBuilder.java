/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.builder;

import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;
import org.opendaylight.yangtools.leafrefcontext.api.LeafRefContext;
import org.opendaylight.yangtools.leafrefcontext.utils.LeafRefUtils;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import java.io.IOException;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import org.opendaylight.yangtools.leafref.parser.LeafRefPathParserImpl;
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
    private LinkedList<LeafRefContext> leafRefs;

    public LeafRefContextTreeBuilder(SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        this.leafRefs = new LinkedList<LeafRefContext>();
    }

    public LeafRefContext buildLeafRefContextTree() throws IOException,
            YangSyntaxErrorException {
        LeafRefContextBuilder rootBuilder = new LeafRefContextBuilder(schemaContext.getQName(),
                schemaContext.getPath(), schemaContext);

        Set<Module> modules = schemaContext.getModules();
        for (Module module : modules) {
            Collection<DataSchemaNode> childNodes = module.getChildNodes();
            for (DataSchemaNode childNode : childNodes) {
                LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(
                        childNode, module);

                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    rootBuilder.addReferencingChild(childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }
        }

        for (Module module : modules) {
            Collection<DataSchemaNode> childNodes = module.getChildNodes();
            for (DataSchemaNode childNode : childNodes) {
                LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(
                         childNode, module);

                if (childLeafRefContext.hasReferencedByChild()
                        || childLeafRefContext.isReferencedBy()) {
                    rootBuilder.addReferencedByChild(childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }
        }

        //FIXME: it might be useful to merge these subtrees (i.e. referencing and referencedBy subtree)

        return rootBuilder.build();
    }

    private LeafRefContext buildLeafRefContextReferencingTree(
            DataSchemaNode node, Module currentModule) throws IOException, YangSyntaxErrorException {

        LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(
                node.getQName(), node.getPath(), schemaContext);

        if (node instanceof DataNodeContainer) {
            DataNodeContainer dataNodeContainer = (DataNodeContainer) node;
            Collection<DataSchemaNode> childNodes = dataNodeContainer
                    .getChildNodes();

            for (DataSchemaNode childNode : childNodes) {
                LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(
                        childNode,currentModule);

                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(
                            childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }
        } else if(node instanceof ChoiceNode) {

            ChoiceNode choice = (ChoiceNode) node;
            Set<ChoiceCaseNode> cases = choice.getCases();
            // :FIXME choice without case

            for (ChoiceCaseNode caseNode : cases) {
                LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(
                         caseNode, currentModule);

                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(
                            childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }

        } else if (node instanceof LeafSchemaNode || node instanceof LeafListSchemaNode) {

            TypeDefinition<?> type = null;

            if(node instanceof LeafSchemaNode) {
                type = ((LeafSchemaNode) node).getType();
            } else {
                type = ((LeafListSchemaNode) node).getType();
            }

            //FIXME: fix case when type is e.g. typdef -> typedef -> leafref
            if (type instanceof Leafref) {
                Leafref leafrefType = (Leafref) type;
                String leafRefPathString = leafrefType.getPathStatement().toString();

                currentLeafRefContextBuilder.setLeafRefTargetPathString(leafRefPathString);
                currentLeafRefContextBuilder.setReferencing(true);

                LeafRefPathParserImpl leafRefPathParser = new LeafRefPathParserImpl(schemaContext,currentModule,node);

                ByteArrayInputStream leafRefPathInputStream = new ByteArrayInputStream(leafRefPathString.getBytes(Charset.forName("UTF-8")));
                LeafRefPath leafRefPath = leafRefPathParser.parseLeafRefPathSourceToSchemaPath(leafRefPathInputStream);

                currentLeafRefContextBuilder.setLeafRefTargetPath(leafRefPath);

                LeafRefContext currentLeafRefContext = currentLeafRefContextBuilder.build();
                leafRefs.add(currentLeafRefContext);
                return currentLeafRefContext;
            }
        }

        return currentLeafRefContextBuilder.build();
    }


    private LeafRefContext buildLeafRefContextReferencedByTree(
            DataSchemaNode node, Module currentModule) throws IOException, YangSyntaxErrorException {

        LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(
                node.getQName(), node.getPath(), schemaContext);

        if (node instanceof DataNodeContainer) {
            DataNodeContainer dataNodeContainer = (DataNodeContainer) node;
            Collection<DataSchemaNode> childNodes = dataNodeContainer
                    .getChildNodes();

            for (DataSchemaNode childNode : childNodes) {
                LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(
                         childNode,currentModule);

                if (childLeafRefContext.hasReferencedByChild()
                        || childLeafRefContext.isReferencedBy()) {
                    currentLeafRefContextBuilder.addReferencedByChild(
                            childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }
        } else if(node instanceof ChoiceNode) {

            ChoiceNode choice = (ChoiceNode) node;
            Set<ChoiceCaseNode> cases = choice.getCases();

            for (ChoiceCaseNode caseNode : cases) {
                LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(
                         caseNode, currentModule);

                if (childLeafRefContext.hasReferencedByChild()
                        || childLeafRefContext.isReferencedBy()) {
                    currentLeafRefContextBuilder.addReferencedByChild(
                            childLeafRefContext,
                            childLeafRefContext.getCurrentNodeQName());
                }
            }

        } else if (node instanceof LeafSchemaNode || node instanceof LeafListSchemaNode) {

            LinkedList<LeafRefContext> foundLeafRefs = getLeafRefsFor(node, currentModule);
            if (!foundLeafRefs.isEmpty()) {
                currentLeafRefContextBuilder.setReferencedBy(true);
                for(LeafRefContext leafRef : foundLeafRefs) {
                    currentLeafRefContextBuilder.addReferencedByLeafRefCtx(leafRef.getCurrentNodeQName(),leafRef);
                }
            }
        }

        return currentLeafRefContextBuilder.build();
    }


    private LinkedList<LeafRefContext> getLeafRefsFor(DataSchemaNode node, Module module) {
        LeafRefPath nodeXPath = LeafRefUtils.schemaPathToLeafRefPath(node.getPath(), module);

        LinkedList<LeafRefContext> foundLeafRefs = new LinkedList<LeafRefContext>();

        for(LeafRefContext leafref : leafRefs) {
            LeafRefPath leafRefTargetPath = leafref.getAbsoluteLeafRefTargetPath();
            if(leafRefTargetPath.equals(nodeXPath)) {
                foundLeafRefs.add(leafref);
            }
        }

        return foundLeafRefs;
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
