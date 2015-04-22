/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Splitter;
import java.net.URI;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.RefineBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GroupingUtils {
    private static final Logger LOG = LoggerFactory.getLogger(GroupingUtils.class);

    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

    private GroupingUtils() {
    }

    /**
     * Search given modules for grouping by name defined in uses node.
     *
     * @param usesBuilder
     *            builder of uses statement
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @return grouping with given name, never null
     * @throws YangParseException
     *             if no grouping found
     */
    public static GroupingBuilder getTargetGroupingFromModules(final UsesNodeBuilder usesBuilder,
            final Map<URI, NavigableMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final int line = usesBuilder.getLine();

        SchemaPath groupingPath = usesBuilder.getTargetGroupingPath();
        QName groupingName = groupingPath.getPathFromRoot().iterator().next();
        ModuleBuilder dependentModule = BuilderUtils.findModule(groupingName, modules);

        Set<GroupingBuilder> groupings = dependentModule.getGroupingBuilders();
        GroupingBuilder result = findGroupingBuilder(groupings, groupingName.getLocalName());
        if (result != null) {
            return result;
        }

        Builder parent = usesBuilder.getParent();
        while (parent != null) {
            if (parent instanceof DataNodeContainerBuilder) {
                groupings = ((DataNodeContainerBuilder) parent).getGroupingBuilders();
            } else if (parent instanceof RpcDefinitionBuilder) {
                groupings = ((RpcDefinitionBuilder) parent).getGroupings();
            }
            result = findGroupingBuilder(groupings, groupingName.getLocalName());
            if (result == null) {
                parent = parent.getParent();
            } else {
                break;
            }
        }

        if (result == null) {
            throw new YangParseException(module.getName(), line, "Grouping '" + groupingName + "' not found.");
        }
        return result;
    }

    /**
     * Find grouping by name.
     *
     * @param groupings
     *            collection of grouping builders to search
     * @param name
     *            name of grouping
     * @return grouping with given name if present in collection, null otherwise
     */
    private static GroupingBuilder findGroupingBuilder(final Set<GroupingBuilder> groupings, final String name) {
        for (GroupingBuilder grouping : groupings) {
            if (grouping.getQName().getLocalName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    /**
     * Perform refinement of uses target grouping nodes. Uses process has to be
     * already performed.
     *
     * @param usesNode
     *            uses node containing refine statements
     */
    public static void performRefine(final UsesNodeBuilder usesNode) {
        for (RefineBuilder refine : usesNode.getRefines()) {
            String refineTargetPath = refine.getTargetPathString();

            Builder currentNode = usesNode.getParent();
            for (String pathElement : SLASH_SPLITTER.split(refineTargetPath)) {
                if (currentNode instanceof DataNodeContainerBuilder) {
                    currentNode = ((DataNodeContainerBuilder) currentNode).getDataChildByName(pathElement);
                } else if (currentNode instanceof ChoiceBuilder) {
                    currentNode = ((ChoiceBuilder) currentNode).getCaseNodeByName(pathElement);
                }
            }

            DataSchemaNodeBuilder nodeToRefine = (DataSchemaNodeBuilder) currentNode;
            if (nodeToRefine == null) {
                // FIXME: exception replaced with log to avoid breakage when
                // user tries to refine instance of extension (unknown node)

                // throw new YangParseException(refine.getModuleName(),
                // refine.getLine(), "Refine target node '" +
                // refine.getTargetPathString() + "' not found");
                LOG.warn("Error in module {} at line {}: Refine target node {} not found.", refine.getModuleName(),
                        refine.getLine(), refine.getTargetPathString());
                continue;
            }
            RefineUtils.performRefine(nodeToRefine, refine);
            usesNode.addRefineNode(nodeToRefine);
        }
    }

    public static class UsesComparator implements Comparator<UsesNodeBuilder> {
        @Override
        public int compare(final UsesNodeBuilder o1, final UsesNodeBuilder o2) {
            return getElementPosition(o2) - getElementPosition(o1);
        }
    }

    private static int getElementPosition(final UsesNodeBuilder usesNode) {
        int i = 0;
        Builder parent = usesNode.getParent();
        while (!(parent instanceof ModuleBuilder)) {
            parent = parent.getParent();
            i++;
        }
        return i;
    }

}
