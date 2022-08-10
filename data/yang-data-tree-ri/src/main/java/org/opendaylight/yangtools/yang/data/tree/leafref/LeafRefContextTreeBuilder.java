/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class LeafRefContextTreeBuilder {
    private final List<LeafRefContext> leafRefs = new LinkedList<>();
    private final EffectiveModelContext schemaContext;

    LeafRefContextTreeBuilder(final EffectiveModelContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    LeafRefContext buildLeafRefContextTree() throws LeafRefYangSyntaxErrorException {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(schemaContext);
        final LeafRefContextBuilder rootBuilder = new LeafRefContextBuilder(SchemaContext.NAME, SchemaPath.ROOT,
            schemaContext);

        final Collection<? extends Module> modules = schemaContext.getModules();
        for (final Module module : modules) {
            for (final DataSchemaNode childNode : module.getChildNodes()) {
                stack.enterSchemaTree(childNode.getQName());
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(childNode, stack);
                stack.exit();
                if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                    rootBuilder.addReferencingChild(childLeafRefContext, childLeafRefContext.getNodeName());
                }
            }
        }
        for (final Module module : modules) {
            for (final DataSchemaNode childNode : module.getChildNodes()) {
                stack.enterSchemaTree(childNode.getQName());
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(childNode, module,
                        stack);
                stack.exit();
                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    rootBuilder.addReferencedByChild(childLeafRefContext, childLeafRefContext.getNodeName());
                }
            }
        }

        // FIXME: it might be useful to merge these subtrees (i.e. referencing
        // and referencedBy subtree)

        return rootBuilder.build();
    }

    private LeafRefContext buildLeafRefContextReferencingTree(final DataSchemaNode node,
            final SchemaInferenceStack stack) {
        final LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(node.getQName(),
            stack.toSchemaPath(), schemaContext);

        if (node instanceof DataNodeContainer container) {
            for (final DataSchemaNode childNode : container.getChildNodes()) {
                stack.enterSchemaTree(childNode.getQName());
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(childNode, stack);
                stack.exit();
                if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof ChoiceSchemaNode choice) {
            // :FIXME choice without case
            for (final CaseSchemaNode caseNode : choice.getCases()) {
                stack.enterSchemaTree(caseNode.getQName());
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(caseNode, stack);
                stack.exit();
                if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }

        } else if (node instanceof TypedDataSchemaNode typedNode) {
            final TypeDefinition<?> type = typedNode.getType();

            // FIXME: fix case when type is e.g. typedef -> typedef -> leafref
            if (type instanceof LeafrefTypeDefinition leafrefType) {
                final PathExpression path = leafrefType.getPathStatement();
                final LeafRefPathParserImpl leafRefPathParser = new LeafRefPathParserImpl(leafrefType, typedNode);
                final LeafRefPath leafRefPath = leafRefPathParser.parseLeafRefPath(path);

                currentLeafRefContextBuilder.setLeafRefTargetPathString(path.getOriginalString());
                currentLeafRefContextBuilder.setReferencing(true);
                currentLeafRefContextBuilder.setLeafRefTargetPath(leafRefPath);

                final LeafRefContext currentLeafRefContext = currentLeafRefContextBuilder.build();
                leafRefs.add(currentLeafRefContext);
                return currentLeafRefContext;
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private LeafRefContext buildLeafRefContextReferencedByTree(final DataSchemaNode node, final Module currentModule,
            final SchemaInferenceStack stack) {
        final LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(node.getQName(),
                stack.toSchemaPath(), schemaContext);
        if (node instanceof DataNodeContainer container) {
            for (final DataSchemaNode childNode : container.getChildNodes()) {
                stack.enterSchemaTree(childNode.getQName());
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(childNode,
                        currentModule, stack);
                stack.exit();
                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof ChoiceSchemaNode choice) {
            for (final CaseSchemaNode caseNode : choice.getCases()) {
                stack.enterSchemaTree(caseNode.getQName());
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(caseNode, currentModule,
                        stack);
                stack.exit();
                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof LeafSchemaNode || node instanceof LeafListSchemaNode) {
            final List<LeafRefContext> foundLeafRefs = getLeafRefsFor(currentModule, stack);
            if (!foundLeafRefs.isEmpty()) {
                currentLeafRefContextBuilder.setReferencedBy(true);
                for (final LeafRefContext leafRef : foundLeafRefs) {
                    currentLeafRefContextBuilder.addReferencedByLeafRefCtx(leafRef.getNodeName(), leafRef);
                }
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private List<LeafRefContext> getLeafRefsFor(final Module module, final SchemaInferenceStack stack) {
        final LeafRefPath nodeXPath = LeafRefUtils.schemaPathToLeafRefPath(stack.toSchemaPath(), module);
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
