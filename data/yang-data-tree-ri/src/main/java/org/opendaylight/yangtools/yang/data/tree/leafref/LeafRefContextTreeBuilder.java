/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class LeafRefContextTreeBuilder {
    private final List<LeafRefContext> leafRefs = new ArrayList<>();
    private final EffectiveModelContext modelContext;

    LeafRefContextTreeBuilder(final EffectiveModelContext schemaContext) {
        modelContext = schemaContext;
    }

    LeafRefContext buildLeafRefContextTree() {
        final var stack = SchemaInferenceStack.of(modelContext);
        final var rootBuilder = new LeafRefContextBuilder(SchemaContext.NAME, ImmutableList.of(), modelContext);

        final var modules = modelContext.getModules();
        for (final var module : modules) {
            for (var childNode : module.getChildNodes()) {
                stack.enterSchemaTree(childNode.getQName());
                final var childLeafRefContext = buildLeafRefContextReferencingTree(childNode, stack);
                stack.exit();
                if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                    rootBuilder.addReferencingChild(childLeafRefContext, childLeafRefContext.getNodeName());
                }
            }
        }
        for (final var module : modules) {
            for (var childNode : module.getChildNodes()) {
                stack.enterSchemaTree(childNode.getQName());
                final var childLeafRefContext = buildLeafRefContextReferencedByTree(childNode, module, stack);
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
        final var currentLeafRefContextBuilder = new LeafRefContextBuilder(node.getQName(), extractPath(stack),
            modelContext);

        switch (node) {
            case DataNodeContainer container -> {
                for (var childNode : container.getChildNodes()) {
                    stack.enterSchemaTree(childNode.getQName());
                    final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(childNode, stack);
                    stack.exit();
                    if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                        currentLeafRefContextBuilder.addReferencingChild(childLeafRefContext,
                            childLeafRefContext.getNodeName());
                    }
                }
            }
            case ChoiceSchemaNode choice -> {
                // :FIXME choice without case
                for (var caseNode : choice.getCases()) {
                    stack.enterSchemaTree(caseNode.getQName());
                    final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(caseNode, stack);
                    stack.exit();
                    if (childLeafRefContext.hasReferencingChild() || childLeafRefContext.isReferencing()) {
                        currentLeafRefContextBuilder.addReferencingChild(childLeafRefContext,
                            childLeafRefContext.getNodeName());
                    }
                }
            }
            case TypedDataSchemaNode typedNode -> {
                // FIXME: fix case when type is e.g. typedef -> typedef -> leafref
                if (typedNode.typeDefinition() instanceof LeafrefTypeDefinition leafrefType) {
                    final var path = leafrefType.getPathStatement();
                    final var leafRefPathParser = new LeafRefPathParserImpl(leafrefType, typedNode);
                    final var  leafRefPath = leafRefPathParser.parseLeafRefPath(path);

                    currentLeafRefContextBuilder.setLeafRefTargetPathString(path.getOriginalString());
                    currentLeafRefContextBuilder.setReferencing(true);
                    currentLeafRefContextBuilder.setLeafRefTargetPath(leafRefPath);

                    final var currentLeafRefContext = currentLeafRefContextBuilder.build();
                    leafRefs.add(currentLeafRefContext);
                    return currentLeafRefContext;
                }
            }
            default -> {
                // No-op
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private LeafRefContext buildLeafRefContextReferencedByTree(final DataSchemaNode node, final Module currentModule,
            final SchemaInferenceStack stack) {
        final var currentLeafRefContextBuilder = new LeafRefContextBuilder(node.getQName(), extractPath(stack),
            modelContext);
        if (node instanceof DataNodeContainer container) {
            for (var childNode : container.getChildNodes()) {
                stack.enterSchemaTree(childNode.getQName());
                final var childLeafRefContext = buildLeafRefContextReferencedByTree(childNode, currentModule, stack);
                stack.exit();
                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof ChoiceSchemaNode choice) {
            for (var caseNode : choice.getCases()) {
                stack.enterSchemaTree(caseNode.getQName());
                final var childLeafRefContext = buildLeafRefContextReferencedByTree(caseNode, currentModule, stack);
                stack.exit();
                if (childLeafRefContext.hasReferencedChild() || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(childLeafRefContext,
                        childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof LeafSchemaNode || node instanceof LeafListSchemaNode) {
            final var foundLeafRefs = getLeafRefsFor(currentModule, stack);
            if (!foundLeafRefs.isEmpty()) {
                currentLeafRefContextBuilder.setReferencedBy(true);
                for (var leafRef : foundLeafRefs) {
                    currentLeafRefContextBuilder.addReferencedByLeafRefCtx(leafRef.getNodeName(), leafRef);
                }
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private List<LeafRefContext> getLeafRefsFor(final Module module, final SchemaInferenceStack stack) {
        final var nodeXPath = LeafRefUtils.schemaPathToLeafRefPath(extractPath(stack), module);
        final var foundLeafRefs = new ArrayList<LeafRefContext>();
        for (var leafref: leafRefs) {
            final var leafRefTargetPath = leafref.getAbsoluteLeafRefTargetPath();
            if (leafRefTargetPath.equals(nodeXPath)) {
                foundLeafRefs.add(leafref);
            }
        }

        return foundLeafRefs;
    }

    private static ImmutableList<QName> extractPath(final SchemaInferenceStack stack) {
        return stack.isEmpty() ? ImmutableList.of()
            : ImmutableList.copyOf(stack.toSchemaNodeIdentifier().getNodeIdentifiers());
    }
}
