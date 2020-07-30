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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.DerefSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.Steps;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.AxisStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Schema Context Util contains support methods for searching through Schema Context modules for specified schema
 * nodes via Schema Path or Revision Aware XPath. The Schema Context Util is designed as mixin, so it is not
 * instantiable.
 */
public final class SchemaContextUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaContextUtil.class);
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private static final Pattern GROUPS_PATTERN = Pattern.compile("\\[(.*?)\\]");

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
     * Attempt to find a DataSchemaNode based on its path from root, similar to
     * {@link #findDataSchemaNode(SchemaContext, Module, PathExpression)} without requiring an expression.
     *
     * @param context Schema Context
     * @param path Path to search for
     * @return SchemaNode from the end of the Schema Path or {@code null} if the Node is not present.
     * @throws NullPointerException if a any argument is null or if the path contains a null element
     */
    @Beta
    public static SchemaNode findDataSchemaNode(final SchemaContext context, final List<QName> path) {
        return findTargetNode(context, null, YangLocationPath.absolute(
            path.stream().map(YangXPathAxis.CHILD::asStep).collect(Collectors.toList())));
    }

    /**
     * Attempt to find a DataSchemaNode based on its path from root, similar to
     * {@link #findDataSchemaNode(SchemaContext, Module, PathExpression)} without requiring an expression.
     *
     * @param context Schema Context
     * @param path Path to search for
     * @return SchemaNode from the end of the Schema Path or {@code null} if the Node is not present.
     * @throws NullPointerException if a any argument is null or if the path contains a null element
     */
    @Beta
    public static SchemaNode findDataSchemaNode(final SchemaContext context, final QName... path) {
        return findDataSchemaNode(context, Arrays.asList(path));
    }

    /**
     * Method attempts to find DataSchemaNode inside of provided Schema Context
     * and Yang Module accordingly to Non-conditional Revision Aware XPath. The
     * specified Module MUST be present in Schema Context otherwise the
     * operation would fail and return <code>null</code>. <br>
     * The Revision Aware XPath MUST be specified WITHOUT the conditional
     * statement (i.e. without [cond]) in path, because in this state the Schema
     * Context is completely unaware of data state and will be not able to
     * properly resolve XPath. If the XPath contains condition the method will
     * return IllegalArgumentException. <br>
     * If the Revision Aware XPath is correct and desired Data Schema Node is
     * present in Yang module or in depending module in Schema Context the
     * method will return specified Data Schema Node, otherwise the operation
     * will fail and method will return <code>null</code>.
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param nonCondXPath
     *            Non Conditional Revision Aware XPath
     * @return Returns Data Schema Node for specified Schema Context for given
     *         Non-conditional Revision Aware XPath, or <code>null</code> if the
     *         DataSchemaNode is not present in Schema Context.
     * @throws NullPointerException if any of the arguments is null
     * @deprecated Use {@link #findDataTreeSchemaNode(SchemaContext, QNameModule, YangLocationPath)} or
     *             {@link #findDataTreeSchemaNode(SchemaContext, QNameModule, PathExpression)} instead.
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
    @Deprecated
    public static SchemaNode findDataSchemaNode(final SchemaContext context, final Module module,
            final PathExpression nonCondXPath) {
        requireNonNull(context, "context");
        requireNonNull(module, "module");

        final String strXPath = nonCondXPath.getOriginalString();
        checkArgument(strXPath.indexOf('[') == -1, "Revision Aware XPath may not contain a condition");
        if (nonCondXPath.isAbsolute()) {
            return findTargetNode(context, xpathToQNamePath(context, module, strXPath));
        }
        return null;
    }

    @Beta
    public static SchemaNode findDataTreeSchemaNode(final SchemaContext ctx, final QNameModule localModule,
            final YangLocationPath absPath) {
        checkArgument(absPath.isAbsolute(), "Unsupported relative path %s", absPath);
        return findTargetNode(ctx, localModule, absPath);
    }

    @Beta
    public static SchemaNode findDataTreeSchemaNode(final SchemaContext ctx, final QNameModule localModule,
            final PathExpression absPath) {
        final Steps pathSteps = absPath.getSteps();
        if (pathSteps instanceof LocationPathSteps) {
            return findDataTreeSchemaNode(ctx, localModule, ((LocationPathSteps) pathSteps).getLocationPath());
        }

        // We would need a reference schema node and no, we do not want to use SchemaContext in its SchemaNode capacity
        checkArgument(!(pathSteps instanceof DerefSteps), "No reference node for steps %s", pathSteps);

        // We are missing proper API alignment, if this ever triggers
        throw new IllegalStateException("Unsupported path " + pathSteps);
    }

    /**
     * Method attempts to find DataSchemaNode inside of provided Schema Context
     * and Yang Module accordingly to Non-conditional relative Revision Aware
     * XPath. The specified Module MUST be present in Schema Context otherwise
     * the operation would fail and return <code>null</code>. <br>
     * The relative Revision Aware XPath MUST be specified WITHOUT the
     * conditional statement (i.e. without [cond]) in path, because in this
     * state the Schema Context is completely unaware of data state and will be
     * not able to properly resolve XPath. If the XPath contains condition the
     * method will return IllegalArgumentException. <br>
     * The Actual Schema Node MUST be specified correctly because from this
     * Schema Node will search starts. If the Actual Schema Node is not correct
     * the operation will simply fail, because it will be unable to find desired
     * DataSchemaNode. <br>
     * If the Revision Aware XPath doesn't have flag
     * <code>isAbsolute == false</code> the method will throw
     * IllegalArgumentException. <br>
     * If the relative Revision Aware XPath is correct and desired Data Schema
     * Node is present in Yang module or in depending module in Schema Context
     * the method will return specified Data Schema Node, otherwise the
     * operation will fail and method will return <code>null</code>.
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param actualSchemaNode
     *            Actual Schema Node
     * @param relativeXPath
     *            Relative Non Conditional Revision Aware XPath
     * @return DataSchemaNode if is present in specified Schema Context for
     *         given relative Revision Aware XPath, otherwise will return
     *         <code>null</code>.
     * @throws NullPointerException if any argument is null
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
    // FIXME: YANGTOOLS-1052: this is a static analysis util, move it to a dedicated class
    public static SchemaNode findDataSchemaNodeForRelativeXPath(final SchemaContext context, final Module module,
            final SchemaNode actualSchemaNode, final PathExpression relativeXPath) {
        checkState(!relativeXPath.isAbsolute(), "Revision Aware XPath MUST be relative i.e. MUST contains ../, "
                + "for non relative Revision Aware XPath use findDataSchemaNode method");
        return resolveRelativeXPath(context, module, removePredicatesFromXpath(relativeXPath.getOriginalString()),
                actualSchemaNode);
    }

    private static String removePredicatesFromXpath(final String xpath) {
        return GROUPS_PATTERN.matcher(xpath).replaceAll("");
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
        final QName qname = schemaNode.getPath().getLastComponent();
        checkState(qname != null, "Schema Path contains invalid state of path parts. "
                + "The Schema Path MUST contain at least ONE QName  which defines namespace and Local name of path.");
        return context.findModule(qname.getModule()).orElse(null);
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
            foundNode = ((ChoiceSchemaNode) parent).findCase(current).orElse(null);

            if (foundNode != null && nextPath.iterator().hasNext()) {
                foundNode = findNodeIn(foundNode, nextPath);
            }

            if (foundNode == null) {
                // fallback that tries to map into one of the child cases
                for (final CaseSchemaNode caseNode : ((ChoiceSchemaNode) parent).getCases()) {
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
     * Transforms string representation of XPath to Queue of QNames. The XPath
     * is split by "/" and for each part of XPath is assigned correct module in
     * Schema Path. <br>
     * If Schema Context, Parent Module or XPath string contains
     * <code>null</code> values, the method will throws IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param parentModule
     *            Parent Module
     * @param xpath
     *            XPath String
     * @return return a list of QName
     */
    private static List<QName> xpathToQNamePath(final SchemaContext context, final Module parentModule,
            final String xpath) {
        final List<QName> path = new ArrayList<>();
        for (final String pathComponent : SLASH_SPLITTER.split(xpath)) {
            path.add(stringPathPartToQName(context, parentModule, pathComponent));
        }
        return path;
    }

    /**
     * Transforms part of Prefixed Path as java String to QName. <br>
     * If the string contains module prefix separated by ":" (i.e.
     * mod:container) this module is provided from from Parent Module list of
     * imports. If the Prefixed module is present in Schema Context the QName
     * can be constructed. <br>
     * If the Prefixed Path Part does not contains prefix the Parent's Module
     * namespace is taken for construction of QName. <br>
     *
     * @param context Schema Context
     * @param parentModule Parent Module
     * @param prefixedPathPart Prefixed Path Part string
     * @return QName from prefixed Path Part String.
     * @throws NullPointerException if any arguments are null
     */
    private static QName stringPathPartToQName(final SchemaContext context, final Module parentModule,
            final String prefixedPathPart) {
        requireNonNull(context, "context");

        if (prefixedPathPart.indexOf(':') != -1) {
            final Iterator<String> prefixedName = COLON_SPLITTER.split(prefixedPathPart).iterator();
            final String modulePrefix = prefixedName.next();

            final Module module = resolveModuleForPrefix(context, parentModule, modulePrefix);
            checkArgument(module != null, "Failed to resolve xpath: no module found for prefix %s in module %s",
                    modulePrefix, parentModule.getName());

            return QName.create(module.getQNameModule(), prefixedName.next());
        }

        return QName.create(parentModule.getNamespace(), parentModule.getRevision(), prefixedPathPart);
    }

    /**
     * Method will attempt to resolve and provide Module reference for specified module prefix. Each Yang module could
     * contains multiple imports which MUST be associated with corresponding module prefix. The method simply looks into
     * module imports and returns the module that is bounded with specified prefix. If the prefix is not present
     * in module or the prefixed module is not present in specified Schema Context, the method will return {@code null}.
     * <br>
     * If String prefix is the same as prefix of the specified Module the reference to this module is returned.<br>
     *
     * @param context Schema Context
     * @param module Yang Module
     * @param prefix Module Prefix
     * @return Module for given prefix in specified Schema Context if is present, otherwise returns <code>null</code>
     * @throws NullPointerException if any arguments are null
     */
    private static Module resolveModuleForPrefix(final SchemaContext context, final Module module,
            final String prefix) {
        requireNonNull(context, "context");

        if (prefix.equals(module.getPrefix())) {
            return module;
        }

        for (final ModuleImport mi : module.getImports()) {
            if (prefix.equals(mi.getPrefix())) {
                return context.findModule(mi.getModuleName(), mi.getRevision()).orElse(null);
            }
        }
        return null;
    }

    /**
     * Resolve a relative XPath into a set of QNames.
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param pathStr
     *            xPath of leafref
     * @param actualSchemaNode
     *            actual schema node
     * @return target schema node
     * @throws IllegalArgumentException if any arguments are null
     */
    private static @Nullable SchemaNode resolveRelativeXPath(final SchemaContext context, final Module module,
            final String pathStr, final SchemaNode actualSchemaNode) {
        checkState(actualSchemaNode.getPath() != null, "Schema Path reference for Leafref cannot be NULL");

        return pathStr.startsWith("deref(") ? resolveDerefPath(context, module, actualSchemaNode, pathStr)
                : findTargetNode(context, resolveRelativePath(context, module, actualSchemaNode,
                    doSplitXPath(pathStr)));
    }

    private static Iterable<QName> resolveRelativePath(final SchemaContext context, final Module module,
            final SchemaNode actualSchemaNode, final List<String> steps) {
        // Find out how many "parent" components there are and trim them
        final int colCount = normalizeXPath(steps);
        final List<String> xpaths = colCount == 0 ? steps : steps.subList(colCount, steps.size());

        final List<QName> walkablePath = createWalkablePath(actualSchemaNode.getPath().getPathFromRoot(),
                context, colCount);

        if (walkablePath == null) {
            return null;
        }

        return Iterables.concat(Iterables.limit(walkablePath, walkablePath.size() - colCount),
            Iterables.transform(xpaths, input -> stringPathPartToQName(context, module, input)));
    }

    /**
     * Return List of qNames that are walkable using xPath. When getting a path from schema node it will return path
     * with parents like CaseSchemaNode and ChoiceSchemaNode as well if they are parents of the node. We need to get
     * rid of these in order to find the node that xPath is pointing to.
     *
     * If path gone behind node method will return {@code null} if root is grouping, otherwise
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param schemaNodePath list of qNames as a path to the leaf of type leafref
     * @param context        create schema context
     * @param colCount       amount of "../" in the xPath expression
     * @return list of QNames as a path where we should be able to find referenced node or {@code null} if path gone
     *         behind grouping node.
     * @throws IllegalArgumentException if path gone behind root node and this node isn't grouping
     */
    private static List<QName> createWalkablePath(final Iterable<QName> schemaNodePath, final SchemaContext context,
            final int colCount) {
        final List<Integer> indexToRemove = new ArrayList<>();
        SchemaNode nodeIn = null;
        List<QName> schemaNodePathRet = Lists.newArrayList(schemaNodePath);
        int i = schemaNodePathRet.size() - 1;
        int j = 0;
        for (; i >= 0 && j < colCount; i--, j++) {
            nodeIn = findTargetNode(context, schemaNodePathRet);
            if (nodeIn instanceof CaseSchemaNode || nodeIn instanceof ChoiceSchemaNode) {
                indexToRemove.add(i);
                j--;
            }
            schemaNodePathRet.remove(i);
        }
        if (i == -1 && j < colCount) {
            checkArgument(nodeIn instanceof GroupingDefinition, "Path gone behind root");
            return null;
        }
        schemaNodePathRet = Lists.newArrayList(schemaNodePath);
        for (int k : indexToRemove) {
            schemaNodePathRet.remove(k);
        }
        return schemaNodePathRet;
    }

    private static SchemaNode resolveDerefPath(final SchemaContext context, final Module module,
            final SchemaNode actualSchemaNode, final String xpath) {
        final int paren = xpath.indexOf(')', 6);
        checkArgument(paren != -1, "Cannot find matching parentheses in %s", xpath);

        final String derefArg = xpath.substring(6, paren).strip();
        // Look up the node which we need to reference
        final SchemaNode derefTarget = findTargetNode(context, resolveRelativePath(context, module, actualSchemaNode,
            doSplitXPath(derefArg)));
        checkArgument(derefTarget != null, "Cannot find deref(%s) target node %s in context of %s", derefArg,
                actualSchemaNode);
        checkArgument(derefTarget instanceof TypedDataSchemaNode, "deref(%s) resolved to non-typed %s", derefArg,
            derefTarget);

        // We have a deref() target, decide what to do about it
        final TypeDefinition<?> targetType = ((TypedDataSchemaNode) derefTarget).getType();
        if (targetType instanceof InstanceIdentifierTypeDefinition) {
            // Static inference breaks down, we cannot determine where this points to
            // FIXME: dedicated exception, users can recover from it, derive from IAE
            throw new UnsupportedOperationException("Cannot infer instance-identifier reference " + targetType);
        }

        // deref() is define only for instance-identifier and leafref types, handle the latter
        checkArgument(targetType instanceof LeafrefTypeDefinition, "Illegal target type %s", targetType);

        final PathExpression targetPath = ((LeafrefTypeDefinition) targetType).getPathStatement();
        LOG.debug("Derefencing path {}", targetPath);

        final SchemaNode deref = targetPath.isAbsolute()
                ? findTargetNode(context, actualSchemaNode.getQName().getModule(),
                    ((LocationPathSteps) targetPath.getSteps()).getLocationPath())
                        : findDataSchemaNodeForRelativeXPath(context, module, actualSchemaNode, targetPath);
        if (deref == null) {
            LOG.debug("Path {} could not be derefenced", targetPath);
            return null;
        }

        checkArgument(deref instanceof LeafSchemaNode, "Unexpected %s reference in %s", deref, targetPath);

        final List<String> qnames = doSplitXPath(xpath.substring(paren + 1).stripLeading());
        return findTargetNode(context, resolveRelativePath(context, module, deref, qnames));
    }

    private static @Nullable SchemaNode findTargetNode(final SchemaContext context, final QNameModule localNamespace,
            final YangLocationPath path) {
        final Deque<QName> ret = new ArrayDeque<>();
        for (Step step : path.getSteps()) {
            if (step instanceof AxisStep) {
                // We only support parent axis steps
                final YangXPathAxis axis = ((AxisStep) step).getAxis();
                checkState(axis == YangXPathAxis.PARENT, "Unexpected axis %s", axis);
                ret.removeLast();
                continue;
            }

            // This has to be a QNameStep
            checkState(step instanceof QNameStep, "Unhandled step %s in %s", step, path);
            ret.addLast(resolve(((QNameStep) step).getQName(), localNamespace));
        }

        return findTargetNode(context, ret);
    }

    private static @Nullable SchemaNode findTargetNode(final SchemaContext context,
            final @Nullable Iterable<QName> qnamePath) {
        if (qnamePath == null) {
            // happens when xpath gone behind root node ant this node it's grouping
            return null;
        }
        // We do not have enough information about resolution context, hence cannot account for actions, RPCs
        // and notifications. We therefore attempt to make a best estimate, but this can still fail.
        final Optional<DataSchemaNode> pureData = context.findDataTreeChild(qnamePath);
        return pureData.isPresent() ? pureData.get() : findNodeInSchemaContext(context, qnamePath);
    }

    private static QName resolve(final AbstractQName toResolve, final QNameModule localNamespace) {
        if (toResolve instanceof QName) {
            return (QName) toResolve;
        } else if (toResolve instanceof UnqualifiedQName) {
            return ((UnqualifiedQName) toResolve).bindTo(localNamespace);
        } else {
            throw new IllegalStateException("Unhandled step " + toResolve);
        }
    }

    @VisibleForTesting
    static int normalizeXPath(final List<String> xpath) {
        LOG.trace("Normalize {}", xpath);

        // We need to make multiple passes here, as the leading XPaths as we can have "../abc/../../def", which really
        // is "../../def"
        while (true) {
            // Next up: count leading ".." components
            int leadingParents = 0;
            while (true) {
                if (leadingParents == xpath.size()) {
                    return leadingParents;
                }
                if (!"..".equals(xpath.get(leadingParents))) {
                    break;
                }

                ++leadingParents;
            }

            // Now let's see if there there is a '..' in the rest
            final int dots = findDots(xpath, leadingParents + 1);
            if (dots == -1) {
                return leadingParents;
            }

            xpath.remove(dots - 1);
            xpath.remove(dots - 1);
            LOG.trace("Next iteration {}", xpath);
        }
    }

    private static int findDots(final List<String> xpath, final int startIndex) {
        for (int i = startIndex; i < xpath.size(); ++i) {
            if ("..".equals(xpath.get(i))) {
                return i;
            }
        }

        return -1;
    }

    private static List<String> doSplitXPath(final String xpath) {
        final List<String> ret = new ArrayList<>();
        for (String str : SLASH_SPLITTER.split(xpath)) {
            ret.add(str);
        }
        return ret;
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
    public static TypeDefinition<?> getBaseTypeForLeafRef(final LeafrefTypeDefinition typeDefinition,
            final SchemaContext schemaContext, final SchemaNode schema) {
        final PathExpression pathStatement = typeDefinition.getPathStatement();
        final String pathStr = stripConditionsFromXPathString(pathStatement);

        final DataSchemaNode dataSchemaNode;
        if (pathStatement.isAbsolute()) {
            SchemaNode baseSchema = schema;
            while (baseSchema instanceof DerivableSchemaNode) {
                final Optional<? extends SchemaNode> basePotential = ((DerivableSchemaNode) baseSchema).getOriginal();
                if (basePotential.isPresent()) {
                    baseSchema = basePotential.get();
                } else {
                    break;
                }
            }

            final Module parentModule = findParentModuleOfReferencingType(schemaContext, baseSchema);
            dataSchemaNode = (DataSchemaNode) findTargetNode(schemaContext,
                xpathToQNamePath(schemaContext, parentModule, pathStr));
        } else {
            Module parentModule = findParentModule(schemaContext, schema);
            dataSchemaNode = (DataSchemaNode) resolveRelativeXPath(schemaContext, parentModule, pathStr, schema);
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

    /**
     * Returns base type for {@code typeDefinition} which belongs to module specified via {@code qname}. This handle
     * the case when leafref type isn't specified as type substatement of leaf or leaf-list but is defined in other
     * module as typedef which is then imported to referenced module.
     *
     * <p>
     * Because {@code typeDefinition} is definied via typedef statement, only absolute path is meaningful.
     */
    public static TypeDefinition<?> getBaseTypeForLeafRef(final LeafrefTypeDefinition typeDefinition,
            final SchemaContext schemaContext, final QName qname) {
        final PathExpression pathStatement = typeDefinition.getPathStatement();
        if (!pathStatement.isAbsolute()) {
            return null;
        }

        final Optional<Module> parentModule = schemaContext.findModule(qname.getModule());
        checkArgument(parentModule.isPresent(), "Failed to find parent module for %s", qname);

        final DataSchemaNode dataSchemaNode = (DataSchemaNode) findTargetNode(schemaContext,
            xpathToQNamePath(schemaContext, parentModule.get(), stripConditionsFromXPathString(pathStatement)));
        final TypeDefinition<?> targetTypeDefinition = typeDefinition(dataSchemaNode);
        if (targetTypeDefinition instanceof LeafrefTypeDefinition) {
            return getBaseTypeForLeafRef((LeafrefTypeDefinition) targetTypeDefinition, schemaContext, dataSchemaNode);
        }

        return targetTypeDefinition;
    }

    private static Module findParentModuleOfReferencingType(final SchemaContext schemaContext,
            final SchemaNode schemaNode) {
        checkArgument(schemaContext != null, "Schema Context reference cannot be NULL!");
        checkArgument(schemaNode instanceof TypedDataSchemaNode, "Unsupported node %s", schemaNode);

        TypeDefinition<?> nodeType = ((TypedDataSchemaNode) schemaNode).getType();
        if (nodeType.getBaseType() != null) {
            while (nodeType.getBaseType() != null) {
                nodeType = nodeType.getBaseType();
            }

            return schemaContext.findModule(nodeType.getQName().getModule()).orElse(null);
        }

        return findParentModule(schemaContext, schemaNode);
    }

    private static final Pattern STRIP_PATTERN = Pattern.compile("\\[[^\\[\\]]*\\]");

    /**
     * Removes conditions from xPath pointed to target node.
     *
     * @param pathStatement
     *            xPath to target node
     * @return string representation of xPath without conditions
     */
    @VisibleForTesting
    static String stripConditionsFromXPathString(final PathExpression pathStatement) {
        return STRIP_PATTERN.matcher(pathStatement.getOriginalString()).replaceAll("");
    }

    /**
     * Gets the base type of DataSchemaNode value.
     *
     * @param node
     *            a node representing DataSchemaNode
     * @return concrete type definition of node value
     */
    private static TypeDefinition<?> typeDefinition(final DataSchemaNode node) {
        checkArgument(node instanceof TypedDataSchemaNode, "Unhandled parameter type %s", node);

        TypeDefinition<?> current = ((TypedDataSchemaNode) node).getType();
        // TODO: don't we have a utility for this?
        TypeDefinition<?> base = current.getBaseType();
        while (base != null) {
            current = base;
            base = current.getBaseType();
        }
        return current;
    }
}
