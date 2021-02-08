/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
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
        // Hidden on purpose
    }

    /**
     * Method attempts to find DataSchemaNode in Schema Context via specified Schema Path. The returned DataSchemaNode
     * from method will be the node at the end of the SchemaPath. If the DataSchemaNode is not present in the Schema
     * Context the method will return {@code null}.
     *
     * <p>
     * In case that Schema Context or Schema Path are not specified correctly (i.e. contains {@code null} values) the
     * method will throw IllegalArgumentException.
     *
     * @param context Schema Context
     * @param schemaPath Schema Path to search for
     * @return SchemaNode from the end of the Schema Path or {@code null} if the Node is not present.
     * @throws NullPointerException if context or schemaPath is null
     */
    public static SchemaNode findDataSchemaNode(final SchemaContext context, final SchemaPath schemaPath) {
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
     * operation will return <code>null</code>.
     *
     * @param context Schema Context
     * @param schemaNode Schema Node
     * @return Yang Module for specified Schema Context and Schema Node, if Schema Node is NOT present, the method will
     *         return <code>null</code>
     * @throws NullPointerException if any of the arguments is null
     */
    public static Module findParentModule(final SchemaContext context, final SchemaNode schemaNode) {
        return context.findModule(schemaNode.getQName().getModule()).orElse(null);
    }

    public static SchemaNode findNodeInSchemaContext(final SchemaContext context, final Iterable<QName> path) {
        final QName current = path.iterator().next();

        LOG.trace("Looking up module {} in context {}", current, path);
        final Optional<Module> module = context.findModule(current.getModule());
        if (module.isEmpty()) {
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
    public static @Nullable ContainerLike getRpcDataSchema(final @NonNull SchemaContext schema,
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

            for (Submodule submodule : module.getSubmodules()) {
                ret.add(moduleToIdentifier(submodule));
            }
        }

        return ret;
    }

    private static SourceIdentifier moduleToIdentifier(final ModuleLike module) {
        return RevisionSourceIdentifier.create(module.getName(), module.getRevision());
    }

    private static SchemaNode findNodeInModule(final Module module, final Iterable<QName> path) {
        if (!path.iterator().hasNext()) {
            LOG.debug("No node matching {} found in node {}", path, module);
            return null;
        }

        final QName current = path.iterator().next();
        LOG.trace("Looking for node {} in module {}", current, module);

        SchemaNode foundNode = null;
        final Iterable<QName> nextPath = nextLevel(path);

        foundNode = module.dataChildByName(current);
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

            foundNode = parentDataNodeContainer.dataChildByName(current);
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
            final Optional<? extends SchemaNode> next = ((ActionNodeContainer) parent).getActions().stream()
                .filter(act -> current.equals(act.getQName())).findFirst();
            if (next.isPresent() && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(next.orElseThrow(), nextPath);
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
            foundNode = ((ChoiceSchemaNode) parent).findCase(current).orElse(null);

            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }

            if (foundNode == null) {
                // fallback that tries to map into one of the child cases
                for (final CaseSchemaNode caseNode : ((ChoiceSchemaNode) parent).getCases()) {
                    final DataSchemaNode maybeChild = caseNode.dataChildByName(current);
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

}
