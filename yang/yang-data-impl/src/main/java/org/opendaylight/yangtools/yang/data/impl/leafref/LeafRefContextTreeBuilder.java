/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

final class LeafRefContextTreeBuilder {
    private final List<LeafRefContext> leafRefs = new LinkedList<>();
    private final SchemaContext schemaContext;

    LeafRefContextTreeBuilder(final SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    LeafRefContext buildLeafRefContextTree() throws LeafRefYangSyntaxErrorException {
        final LeafRefContextBuilder rootBuilder = new LeafRefContextBuilder(schemaContext.getQName(),
            schemaContext.getPath(), schemaContext);

        final Set<Module> modules = schemaContext.getModules();
        for (final Module module : modules) {
            for (final DataSchemaNode childNode : module.getChildNodes()) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(childNode, module);
                if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                    rootBuilder.addReferencingChild(childLeafRefContext, childLeafRefContext.getNodeName());
                }
            }
        }

        for (final Module module : modules) {
            final Collection<DataSchemaNode> childNodes = module.getChildNodes();
            for (final DataSchemaNode childNode : childNodes) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(childNode, module);

                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    rootBuilder.addReferencedByChild(childLeafRefContext, childLeafRefContext.getNodeName());
                }
            }
        }

        // FIXME: it might be useful to merge these subtrees (i.e. referencing
        // and referencedBy subtree)

        return rootBuilder.build();
    }

    private LeafRefContext buildLeafRefContextReferencingTree(final DataSchemaNode node, final Module currentModule)
            throws LeafRefYangSyntaxErrorException {
        final LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(node.getQName(),
            node.getPath(), schemaContext);

        if (node instanceof DataNodeContainer) {
            for (final DataSchemaNode childNode : ((DataNodeContainer) node).getChildNodes()) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(childNode, currentModule);
                if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof ChoiceSchemaNode) {
            // :FIXME choice without case
            for (final CaseSchemaNode caseNode : ((ChoiceSchemaNode) node).getCases().values()) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(caseNode, currentModule);
                if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }

        } else if (node instanceof TypedDataSchemaNode) {
            final TypeDefinition<?> type = ((TypedDataSchemaNode) node).getType();

            // FIXME: fix case when type is e.g. typedef -> typedef -> leafref
            if (type instanceof LeafrefTypeDefinition) {
                final LeafrefTypeDefinition leafrefType = (LeafrefTypeDefinition) type;
                final PathExpression leafRefPath = leafrefType.getPathStatement();
                final LeafRefPath leafRefPath = leafRefPathParser.parseLeafRefPath(leafRefPathString);

                currentLeafRefContextBuilder.setLeafRefTargetPathString(leafRefPathString);
                currentLeafRefContextBuilder.setReferencing(true);
                currentLeafRefContextBuilder.setLeafRefTargetPath(leafRefPath);

                final LeafRefContext currentLeafRefContext = currentLeafRefContextBuilder.build();
                leafRefs.add(currentLeafRefContext);
                return currentLeafRefContext;
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private LeafRefContext buildLeafRefContextReferencedByTree(final DataSchemaNode node, final Module currentModule)
            throws LeafRefYangSyntaxErrorException {
        final LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(node.getQName(),
            node.getPath(), schemaContext);
        if (node instanceof DataNodeContainer) {
            for (final DataSchemaNode childNode : ((DataNodeContainer) node).getChildNodes()) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(childNode,
                    currentModule);
                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof ChoiceSchemaNode) {
            for (final CaseSchemaNode caseNode : ((ChoiceSchemaNode) node).getCases().values()) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(caseNode, currentModule);
                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof LeafSchemaNode || node instanceof LeafListSchemaNode) {
            final List<LeafRefContext> foundLeafRefs = getLeafRefsFor(node, currentModule);
            if (!foundLeafRefs.isEmpty()) {
                currentLeafRefContextBuilder.setReferencedBy(true);
                for (final LeafRefContext leafRef : foundLeafRefs) {
                    currentLeafRefContextBuilder.addReferencedByLeafRefCtx(leafRef.getNodeName(), leafRef);
                }
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private List<LeafRefContext> getLeafRefsFor(final DataSchemaNode node, final Module module) {
        final LeafRefPath nodeXPath = LeafRefUtils.schemaPathToLeafRefPath(node.getPath(), module);
        final List<LeafRefContext> foundLeafRefs = new LinkedList<>();
        for (final LeafRefContext leafref : leafRefs) {
            final LeafRefPath leafRefTargetPath = leafref.getAbsoluteLeafRefTargetPath();
            if (leafRefTargetPath.equals(nodeXPath)) {
                foundLeafRefs.add(leafref);
            }
        }

        return foundLeafRefs;
    }
}
