/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

class LeafRefContextTreeBuilder {
    private final SchemaContext schemaContext;
    private final List<LeafRefContext> leafRefs;

    public LeafRefContextTreeBuilder(final SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
        this.leafRefs = new LinkedList<>();
    }

    public LeafRefContext buildLeafRefContextTree() throws IOException,
            LeafRefYangSyntaxErrorException {
        final LeafRefContextBuilder rootBuilder = new LeafRefContextBuilder(
                schemaContext.getQName(), schemaContext.getPath(),
                schemaContext);

        final Set<Module> modules = schemaContext.getModules();
        for (final Module module : modules) {
            final Collection<DataSchemaNode> childNodes = module.getChildNodes();
            for (final DataSchemaNode childNode : childNodes) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(
                        childNode, module);

                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    rootBuilder.addReferencingChild(childLeafRefContext,
                            childLeafRefContext.getNodeName());
                }
            }
        }

        for (final Module module : modules) {
            final Collection<DataSchemaNode> childNodes = module.getChildNodes();
            for (final DataSchemaNode childNode : childNodes) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(
                        childNode, module);

                if (childLeafRefContext.hasReferencedChild()
                        || childLeafRefContext.isReferenced()) {
                    rootBuilder.addReferencedByChild(childLeafRefContext,
                            childLeafRefContext.getNodeName());
                }
            }
        }

        // FIXME: it might be useful to merge these subtrees (i.e. referencing
        // and referencedBy subtree)

        return rootBuilder.build();
    }

    private LeafRefContext buildLeafRefContextReferencingTree(
            final DataSchemaNode node, final Module currentModule) throws IOException,
            LeafRefYangSyntaxErrorException {

        final LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(
                node.getQName(), node.getPath(), schemaContext);

        if (node instanceof DataNodeContainer) {
            final DataNodeContainer dataNodeContainer = (DataNodeContainer) node;
            final Collection<DataSchemaNode> childNodes = dataNodeContainer
                    .getChildNodes();

            for (final DataSchemaNode childNode : childNodes) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(
                        childNode, currentModule);

                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(
                            childLeafRefContext,
                            childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof ChoiceSchemaNode) {

            final ChoiceSchemaNode choice = (ChoiceSchemaNode) node;
            final Set<ChoiceCaseNode> cases = choice.getCases();
            // :FIXME choice without case

            for (final ChoiceCaseNode caseNode : cases) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencingTree(
                        caseNode, currentModule);

                if (childLeafRefContext.hasReferencingChild()
                        || childLeafRefContext.isReferencing()) {
                    currentLeafRefContextBuilder.addReferencingChild(
                            childLeafRefContext,
                            childLeafRefContext.getNodeName());
                }
            }

        } else if (node instanceof TypedSchemaNode) {
            final TypeDefinition<?> type = ((TypedSchemaNode) node).getType();

            // FIXME: fix case when type is e.g. typedef -> typedef -> leafref
            if (type instanceof LeafrefTypeDefinition) {
                final LeafrefTypeDefinition leafrefType = (LeafrefTypeDefinition) type;
                final String leafRefPathString = leafrefType.getPathStatement().toString();

                currentLeafRefContextBuilder.setLeafRefTargetPathString(leafRefPathString);
                currentLeafRefContextBuilder.setReferencing(true);

                final LeafRefPathParserImpl leafRefPathParser = new LeafRefPathParserImpl(schemaContext,
                        checkNotNull(getBaseTypeModule(leafrefType), "Unable to find base module for leafref %s", node),
                        node);

                final LeafRefPath leafRefPath = leafRefPathParser.parseLeafRefPathSourceToSchemaPath(
                    new ByteArrayInputStream(leafRefPathString.getBytes(StandardCharsets.UTF_8)));

                currentLeafRefContextBuilder.setLeafRefTargetPath(leafRefPath);

                final LeafRefContext currentLeafRefContext = currentLeafRefContextBuilder.build();
                leafRefs.add(currentLeafRefContext);
                return currentLeafRefContext;
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private Module getBaseTypeModule(final LeafrefTypeDefinition leafrefType) {
        /*
         * Find the first definition of supplied leafref type and return the
         * module which contains this definition.
         */
        LeafrefTypeDefinition baseLeafRefType = leafrefType;
        while (baseLeafRefType.getBaseType() != null) {
            baseLeafRefType = baseLeafRefType.getBaseType();
        }
        final QNameModule module = baseLeafRefType.getQName().getModule();
        return schemaContext.findModuleByNamespaceAndRevision(module.getNamespace(), module.getRevision());
    }

    private LeafRefContext buildLeafRefContextReferencedByTree(
            final DataSchemaNode node, final Module currentModule) throws IOException,
            LeafRefYangSyntaxErrorException {

        final LeafRefContextBuilder currentLeafRefContextBuilder = new LeafRefContextBuilder(
                node.getQName(), node.getPath(), schemaContext);

        if (node instanceof DataNodeContainer) {
            final DataNodeContainer dataNodeContainer = (DataNodeContainer) node;
            final Collection<DataSchemaNode> childNodes = dataNodeContainer
                    .getChildNodes();

            for (final DataSchemaNode childNode : childNodes) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(
                        childNode, currentModule);

                if (childLeafRefContext.hasReferencedChild()
                        || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(
                            childLeafRefContext,
                            childLeafRefContext.getNodeName());
                }
            }
        } else if (node instanceof ChoiceSchemaNode) {

            final ChoiceSchemaNode choice = (ChoiceSchemaNode) node;
            final Set<ChoiceCaseNode> cases = choice.getCases();

            for (final ChoiceCaseNode caseNode : cases) {
                final LeafRefContext childLeafRefContext = buildLeafRefContextReferencedByTree(
                        caseNode, currentModule);

                if (childLeafRefContext.hasReferencedChild()
                        || childLeafRefContext.isReferenced()) {
                    currentLeafRefContextBuilder.addReferencedByChild(
                            childLeafRefContext,
                            childLeafRefContext.getNodeName());
                }
            }

        } else if (node instanceof LeafSchemaNode
                || node instanceof LeafListSchemaNode) {

            final List<LeafRefContext> foundLeafRefs = getLeafRefsFor(node,
                    currentModule);
            if (!foundLeafRefs.isEmpty()) {
                currentLeafRefContextBuilder.setReferencedBy(true);
                for (final LeafRefContext leafRef : foundLeafRefs) {
                    currentLeafRefContextBuilder.addReferencedByLeafRefCtx(
                            leafRef.getNodeName(), leafRef);
                }
            }
        }

        return currentLeafRefContextBuilder.build();
    }

    private List<LeafRefContext> getLeafRefsFor(final DataSchemaNode node,
            final Module module) {
        final LeafRefPath nodeXPath = LeafRefUtils.schemaPathToLeafRefPath(
                node.getPath(), module);

        final List<LeafRefContext> foundLeafRefs = new LinkedList<>();

        for (final LeafRefContext leafref : leafRefs) {
            final LeafRefPath leafRefTargetPath = leafref
                    .getAbsoluteLeafRefTargetPath();
            if (leafRefTargetPath.equals(nodeXPath)) {
                foundLeafRefs.add(leafref);
            }
        }

        return foundLeafRefs;
    }

}
