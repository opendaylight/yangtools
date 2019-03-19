/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.AxisStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Schema Context Util contains support methods for searching through Schema Context modules for specified schema
 * nodes via Schema Path or Revision Aware XPath. The Schema Context Util is designed as mixin, so it is not
 * instantiable.
 */
public final class SchemaContextUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaContextUtil.class);

    private SchemaContextUtil() {
    }

    /**
     * Method attempts to find DataSchemaNode in Schema Context via specified
     * Schema Path. The returned DataSchemaNode from method will be the node at
     * the end of the SchemaPath. If the DataSchemaNode is not present in the
     * Schema Context the method will return <code>null</code>. <br>
     * In case that Schema Context or Schema Path are not specified correctly
     * (i.e. contains <code>null</code> values) the method will throw
     * IllegalArgumentException.
     *
     * @param context
     *            Schema Context
     * @param schemaPath
     *            Schema Path to search for
     * @return SchemaNode from the end of the Schema Path or <code>null</code>
     *         if the Node is not present.
     * @throws IllegalArgumentException if context or schemaPath is not correct.
     */
    public static SchemaNode findDataSchemaNode(final SchemaContext context, final SchemaPath schemaPath) {
        checkArgument(context != null, "Schema Context reference cannot be NULL");
        checkArgument(schemaPath != null, "Schema Path reference cannot be NULL");

        final Iterable<QName> prefixedPath = schemaPath.getPathFromRoot();
        if (prefixedPath == null) {
            LOG.debug("Schema path {} has null path", schemaPath);
            return null;
        }

        LOG.trace("Looking for path {} in context {}", schemaPath, context);
        return findNodeInSchemaContext(context, prefixedPath);
    }

    /**
     * Returns parent Yang Module for specified Schema Context in which Schema
     * Node is declared. If the Schema Node is not present in Schema Context the
     * operation will return <code>null</code>. <br>
     * If Schema Context or Schema Node contains <code>null</code> references
     * the method will throw IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param schemaNode
     *            Schema Node
     * @return Yang Module for specified Schema Context and Schema Node, if Schema Node is NOT present, the method will
     *         return <code>null</code>
     */
    public static Module findParentModule(final SchemaContext context, final SchemaNode schemaNode) {
        checkArgument(context != null, "Schema Context reference cannot be NULL!");
        checkArgument(schemaNode != null, "Schema Node cannot be NULL!");
        checkState(schemaNode.getPath() != null, "Schema Path for Schema Node is not set properly (Schema Path is "
                + "NULL)");

        final QName qname = schemaNode.getPath().getLastComponent();
        checkState(qname != null, "Schema Path contains invalid state of path parts. "
                + "The Schema Path MUST contain at least ONE QName  which defines namespace and Local name of path.");
        return context.findModule(qname.getModule()).orElse(null);
    }

    public static SchemaNode findNodeInSchemaContext(final SchemaContext context, final Iterable<QName> path) {
        final QName current = path.iterator().next();

        LOG.trace("Looking up module {} in context {}", current, path);
        final Optional<Module> module = context.findModule(current.getModule());
        if (!module.isPresent()) {
            LOG.debug("Module {} not found", current);
            return null;
        }

        return findNodeInModule(module.get(), path);
    }

    /**
     * Returns NotificationDefinition from Schema Context.
     *
     * @param schema SchemaContext in which lookup should be performed.
     * @param path Schema Path of notification
     * @return Notification schema or null, if notification is not present in schema context.
     */
    @Beta
    public static @Nullable NotificationDefinition getNotificationSchema(final @NonNull SchemaContext schema,
            final @NonNull SchemaPath path) {
        requireNonNull(schema, "Schema context must not be null.");
        requireNonNull(path, "Schema path must not be null.");
        for (final NotificationDefinition potential : schema.getNotifications()) {
            if (path.equals(potential.getPath())) {
                return potential;
            }
        }
        return null;
    }

    /**
     * Returns RPC Input or Output Data container from RPC definition.
     *
     * @param schema SchemaContext in which lookup should be performed.
     * @param path Schema path of RPC input/output data container
     * @return Notification schema or null, if notification is not present in schema context.
     */
    @Beta
    public static @Nullable ContainerSchemaNode getRpcDataSchema(final @NonNull SchemaContext schema,
            final @NonNull SchemaPath path) {
        requireNonNull(schema, "Schema context must not be null.");
        requireNonNull(path, "Schema path must not be null.");
        final Iterator<QName> it = path.getPathFromRoot().iterator();
        checkArgument(it.hasNext(), "Rpc must have QName.");
        final QName rpcName = it.next();
        checkArgument(it.hasNext(), "input or output must be part of path.");
        final QName inOrOut = it.next();
        for (final RpcDefinition potential : schema.getOperations()) {
            if (rpcName.equals(potential.getQName())) {
                return SchemaNodeUtils.getRpcDataSchema(potential, inOrOut);
            }
        }
        return null;
    }

    /**
     * Extract the identifiers of all modules and submodules which were used to create a particular SchemaContext.
     *
     * @param context SchemaContext to be examined
     * @return Set of ModuleIdentifiers.
     */
    public static Set<SourceIdentifier> getConstituentModuleIdentifiers(final SchemaContext context) {
        final Set<SourceIdentifier> ret = new HashSet<>();

        for (Module module : context.getModules()) {
            ret.add(moduleToIdentifier(module));

            for (Module submodule : module.getSubmodules()) {
                ret.add(moduleToIdentifier(submodule));
            }
        }

        return ret;
    }

    private static SourceIdentifier moduleToIdentifier(final Module module) {
        return RevisionSourceIdentifier.create(module.getName(), module.getRevision());
    }

    private static SchemaNode findNodeInModule(final Module module, final Iterable<QName> path) {
        checkArgument(module != null, "Parent reference cannot be NULL");
        checkArgument(path != null, "Path reference cannot be NULL");

        if (!path.iterator().hasNext()) {
            LOG.debug("No node matching {} found in node {}", path, module);
            return null;
        }

        final QName current = path.iterator().next();
        LOG.trace("Looking for node {} in module {}", current, module);

        SchemaNode foundNode = null;
        final Iterable<QName> nextPath = nextLevel(path);

        foundNode = module.getDataChildByName(current);
        if (foundNode != null && nextPath.iterator().hasNext()) {
            foundNode = findNodeIn(foundNode, nextPath);
        }

        if (foundNode == null) {
            foundNode = getGroupingByName(module, current);
            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }
        }

        if (foundNode == null) {
            foundNode = getRpcByName(module, current);
            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }
        }

        if (foundNode == null) {
            foundNode = getNotificationByName(module, current);
            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }
        }

        if (foundNode == null) {
            LOG.debug("No node matching {} found in node {}", path, module);
        }

        return foundNode;

    }

    private static SchemaNode findNodeIn(final SchemaNode parent, final Iterable<QName> path) {
        checkArgument(parent != null, "Parent reference cannot be NULL");
        checkArgument(path != null, "Path reference cannot be NULL");

        if (!path.iterator().hasNext()) {
            LOG.debug("No node matching {} found in node {}", path, parent);
            return null;
        }

        final QName current = path.iterator().next();
        LOG.trace("Looking for node {} in node {}", current, parent);

        SchemaNode foundNode = null;
        final Iterable<QName> nextPath = nextLevel(path);

        if (parent instanceof DataNodeContainer) {
            final DataNodeContainer parentDataNodeContainer = (DataNodeContainer) parent;

            foundNode = parentDataNodeContainer.getDataChildByName(current);
            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }

            if (foundNode == null) {
                foundNode = getGroupingByName(parentDataNodeContainer, current);
                if (foundNode != null && nextPath.iterator().hasNext()) {
                    foundNode = findNodeIn(foundNode, nextPath);
                }
            }
        }

        if (foundNode == null && parent instanceof ActionNodeContainer) {
            foundNode = ((ActionNodeContainer) parent).getActions().stream()
                    .filter(act -> current.equals(act.getQName())).findFirst().orElse(null);
            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }
        }

        if (foundNode == null && parent instanceof NotificationNodeContainer) {
            foundNode = ((NotificationNodeContainer) parent).getNotifications().stream()
                    .filter(notif -> current.equals(notif.getQName())).findFirst().orElse(null);
            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }
        }

        if (foundNode == null && parent instanceof OperationDefinition) {
            final OperationDefinition parentRpcDefinition = (OperationDefinition) parent;

            if (current.getLocalName().equals("input")) {
                foundNode = parentRpcDefinition.getInput();
                if (foundNode != null && nextPath.iterator().hasNext()) {
                    foundNode = findNodeIn(foundNode, nextPath);
                }
            }

            if (current.getLocalName().equals("output")) {
                foundNode = parentRpcDefinition.getOutput();
                if (foundNode != null && nextPath.iterator().hasNext()) {
                    foundNode = findNodeIn(foundNode, nextPath);
                }
            }

            if (foundNode == null) {
                foundNode = getGroupingByName(parentRpcDefinition, current);
                if (foundNode != null && nextPath.iterator().hasNext()) {
                    foundNode = findNodeIn(foundNode, nextPath);
                }
            }
        }

        if (foundNode == null && parent instanceof ChoiceSchemaNode) {
            foundNode = ((ChoiceSchemaNode) parent).getCaseNodeByName(current);

            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }

            if (foundNode == null) {
                // fallback that tries to map into one of the child cases
                for (final CaseSchemaNode caseNode : ((ChoiceSchemaNode) parent).getCases().values()) {
                    final DataSchemaNode maybeChild = caseNode.getDataChildByName(current);
                    if (maybeChild != null) {
                        foundNode = findNodeIn(maybeChild, nextPath);
                        break;
                    }
                }
            }
        }

        if (foundNode == null) {
            LOG.debug("No node matching {} found in node {}", path, parent);
        }

        return foundNode;

    }

    private static Iterable<QName> nextLevel(final Iterable<QName> path) {
        return Iterables.skip(path, 1);
    }

    private static RpcDefinition getRpcByName(final Module module, final QName name) {
        for (final RpcDefinition rpc : module.getRpcs()) {
            if (rpc.getQName().equals(name)) {
                return rpc;
            }
        }
        return null;
    }

    private static NotificationDefinition getNotificationByName(final Module module, final QName name) {
        for (final NotificationDefinition notification : module.getNotifications()) {
            if (notification.getQName().equals(name)) {
                return notification;
            }
        }
        return null;
    }

    private static GroupingDefinition getGroupingByName(final DataNodeContainer dataNodeContainer, final QName name) {
        for (final GroupingDefinition grouping : dataNodeContainer.getGroupings()) {
            if (grouping.getQName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    private static GroupingDefinition getGroupingByName(final OperationDefinition rpc, final QName name) {
        for (final GroupingDefinition grouping : rpc.getGroupings()) {
            if (grouping.getQName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    /**
     * Extracts the base type of node on which schema node points to. If target node is again of type
     * LeafrefTypeDefinition, methods will be call recursively until it reach concrete type definition.
     *
     * @param typeDefinition
     *            type of node which will be extracted
     * @param schemaContext
     *            Schema Context
     * @param schema
     *            Schema Node
     * @return recursively found type definition this leafref is pointing to or null if the xpath is incorrect (null
     *         is there to preserve backwards compatibility)
     */
    // FIXME: This entire method is ill-defined, as the resolution process depends on  where the XPath is defined --
    //        notably RPCs, actions and notifications modify the data tree temporarily. See sections 6.4.1 and 9.9.2
    //        of RFC7950.
    //
    //        Most notably we need to understand whether the XPath is being resolved in the data tree, or as part of
    //        a notification/action/RPC, as then the SchemaContext grows tentative nodes ... which could be addressed
    //        via a derived SchemaContext (i.e. this class would have to have a
    //
    //            SchemaContext notificationSchemaContext(SchemaContext delegate, NotificationDefinition notif)
    //
    //        which would then be passed in to a method similar to this one. In static contexts, like MD-SAL codegen,
    //        that feels like an overkill.
    public static TypeDefinition<?> getBaseTypeForLeafRef(final LeafrefTypeDefinition typeDefinition,
            final SchemaContext schemaContext, final SchemaNode schema) {
        final YangLocationPath pathStatement = typeDefinition.getPathStatement().getLocation();

        final DataSchemaNode dataSchemaNode;
        if (!pathStatement.isAbsolute()) {
            final Deque<QName> stack = new ArrayDeque<>();
            schema.getPath().getPathTowardsRoot().forEach(stack::addLast);
            dataSchemaNode = findBaseTypeForLeafref(schemaContext, stack, pathStatement.getSteps());
        } else {
            dataSchemaNode = findBaseTypeForLeafref(schemaContext, new ArrayDeque<>(), pathStatement.getSteps());
        }

        // FIXME this is just to preserve backwards compatibility since yangtools do not mind wrong leafref xpaths
        // and current expected behaviour for such cases is to just use pure string
        // This should throw an exception about incorrect XPath in leafref
        if (dataSchemaNode == null) {
            return null;
        }

        final TypeDefinition<?> targetTypeDefinition = typeDefinition(dataSchemaNode);

        if (targetTypeDefinition instanceof LeafrefTypeDefinition) {
            return getBaseTypeForLeafRef((LeafrefTypeDefinition) targetTypeDefinition, schemaContext, dataSchemaNode);
        }

        return targetTypeDefinition;
    }

    private static DataSchemaNode findBaseTypeForLeafref(final SchemaContext schemaContext,
            final Deque<QName> stack, final ImmutableList<Step> steps) {
        // Steps are greatly reduced: they can either be parent axis up, or child axis QNames, we are ignoring
        // predicates
        for (Step step : steps) {
            switch (step.getAxis()) {
                case PARENT:
                    checkState(step instanceof AxisStep, "Unhandled parent step %s", step);
                    stack.pop();
                    break;
                case CHILD:
                    checkState(step instanceof QNameStep, "Unhandled child step %s", step);
                    stack.push(((QNameStep) step).getQName());
                    break;
                default:
                    throw new IllegalStateException("Unhandled step " + step);
            }
        }

        // Convert to a path from root
        final List<QName> path = new ArrayList<>(stack.size());
        stack.descendingIterator().forEachRemaining(path::add);

        // We do not have enough information about resolution context, hence cannot account for actions, RPCs
        // and notifications. We therefore attempt to make a best estimate, but this can still fail.
        final Optional<DataSchemaNode> pureData = schemaContext.findDataTreeChild(path);
        return pureData.isPresent() ? pureData.get() : (DataSchemaNode) findNodeInSchemaContext(schemaContext, path);
    }

    /**
     * Extracts the base type of leaf schema node until it reach concrete type of TypeDefinition.
     *
     * @param node
     *            a node representing LeafSchemaNode
     * @return concrete type definition of node value
     */
    private static TypeDefinition<?> typeDefinition(final LeafSchemaNode node) {
        TypeDefinition<?> baseType = node.getType();
        while (baseType.getBaseType() != null) {
            baseType = baseType.getBaseType();
        }
        return baseType;
    }

    /**
     * Extracts the base type of leaf schema node until it reach concrete type of TypeDefinition.
     *
     * @param node
     *            a node representing LeafListSchemaNode
     * @return concrete type definition of node value
     */
    private static TypeDefinition<?> typeDefinition(final LeafListSchemaNode node) {
        TypeDefinition<?> baseType = node.getType();
        while (baseType.getBaseType() != null) {
            baseType = baseType.getBaseType();
        }
        return baseType;
    }

    /**
     * Gets the base type of DataSchemaNode value.
     *
     * @param node
     *            a node representing DataSchemaNode
     * @return concrete type definition of node value
     */
    private static TypeDefinition<?> typeDefinition(final DataSchemaNode node) {
        if (node instanceof LeafListSchemaNode) {
            return typeDefinition((LeafListSchemaNode) node);
        } else if (node instanceof LeafSchemaNode) {
            return typeDefinition((LeafSchemaNode) node);
        } else {
            throw new IllegalArgumentException("Unhandled parameter type: " + node);
        }
    }
}
