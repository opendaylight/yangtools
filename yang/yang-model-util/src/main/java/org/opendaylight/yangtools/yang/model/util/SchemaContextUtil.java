/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Schema Context Util contains support methods for searching through Schema
 * Context modules for specified schema nodes via Schema Path or Revision Aware
 * XPath. The Schema Context Util is designed as mixin, so it is not
 * instantiable.
 *
 */
public final class SchemaContextUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaContextUtil.class);
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Splitter SLASH_SPLITTER = Splitter.on('/');

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
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(schemaPath != null, "Schema Path reference cannot be NULL");

        final Iterable<QName> prefixedPath = schemaPath.getPathFromRoot();
        if (prefixedPath == null) {
            LOG.debug("Schema path {} has null path", schemaPath);
            return null;
        }

        LOG.trace("Looking for path {} in context {}", schemaPath, context);
        return findNodeInSchemaContext(context, prefixedPath);
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
     * In case that Schema Context or Module or Revision Aware XPath contains
     * <code>null</code> references the method will throw
     * IllegalArgumentException <br>
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
     */
    public static SchemaNode findDataSchemaNode(final SchemaContext context, final Module module,
            final RevisionAwareXPath nonCondXPath) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(nonCondXPath != null, "Non Conditional Revision Aware XPath cannot be NULL");

        final String strXPath = nonCondXPath.toString();
        if (strXPath != null) {
            Preconditions.checkArgument(strXPath.indexOf('[') == -1,
                    "Revision Aware XPath may not contain a condition");
            if (nonCondXPath.isAbsolute()) {
                final List<QName> qnamedPath = xpathToQNamePath(context, module, strXPath);
                if (qnamedPath != null) {
                    return findNodeInSchemaContext(context, qnamedPath);
                }
            }
        }
        return null;
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
     * In case that Schema Context or Module or Actual Schema Node or relative
     * Revision Aware XPath contains <code>null</code> references the method
     * will throw IllegalArgumentException <br>
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
     */
    public static SchemaNode findDataSchemaNodeForRelativeXPath(final SchemaContext context, final Module module,
            final SchemaNode actualSchemaNode, final RevisionAwareXPath relativeXPath) {
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(actualSchemaNode != null, "Actual Schema Node reference cannot be NULL");
        Preconditions.checkArgument(relativeXPath != null, "Non Conditional Revision Aware XPath cannot be NULL");
        Preconditions.checkState(!relativeXPath.isAbsolute(),
                "Revision Aware XPath MUST be relative i.e. MUST contains ../, "
                        + "for non relative Revision Aware XPath use findDataSchemaNode method");

        final SchemaPath actualNodePath = actualSchemaNode.getPath();
        if (actualNodePath != null) {
            final Iterable<QName> qnamePath = resolveRelativeXPath(context, module, relativeXPath, actualSchemaNode);

            if (qnamePath != null) {
                return findNodeInSchemaContext(context, qnamePath);
            }
        }
        return null;
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
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL!");
        Preconditions.checkArgument(schemaNode != null, "Schema Node cannot be NULL!");
        Preconditions.checkState(schemaNode.getPath() != null, "Schema Path for Schema Node is not "
                + "set properly (Schema Path is NULL)");

        final QName qname = schemaNode.getPath().getLastComponent();
        Preconditions.checkState(qname != null, "Schema Path contains invalid state of path parts. "
                + "The Schema Path MUST contain at least ONE QName  which defines namespace and Local name of path.");
        return context.findModuleByNamespaceAndRevision(qname.getNamespace(), qname.getRevision().orElse(null));
    }

    public static SchemaNode findNodeInSchemaContext(final SchemaContext context, final Iterable<QName> path) {
        final QName current = path.iterator().next();

        LOG.trace("Looking up module {} in context {}", current, path);
        final Module module = context.findModuleByNamespaceAndRevision(current.getNamespace(),
            current.getRevision().orElse(null));
        if (module == null) {
            LOG.debug("Module {} not found", current);
            return null;
        }

        return findNodeInModule(module, path);
    }

    /**
     * Returns NotificationDefinition from Schema Context.
     *
     * @param schema SchemaContext in which lookup should be performed.
     * @param path Schema Path of notification
     * @return Notification schema or null, if notification is not present in schema context.
     */
    @Beta
    @Nullable
    public static NotificationDefinition getNotificationSchema(@Nonnull final SchemaContext schema,
            @Nonnull final SchemaPath path) {
        Preconditions.checkNotNull(schema, "Schema context must not be null.");
        Preconditions.checkNotNull(path, "Schema path must not be null.");
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
    @Nullable
    public static ContainerSchemaNode getRpcDataSchema(@Nonnull final SchemaContext schema,
            @Nonnull final SchemaPath path) {
        Preconditions.checkNotNull(schema, "Schema context must not be null.");
        Preconditions.checkNotNull(path, "Schema path must not be null.");
        final Iterator<QName> it = path.getPathFromRoot().iterator();
        Preconditions.checkArgument(it.hasNext(), "Rpc must have QName.");
        final QName rpcName = it.next();
        Preconditions.checkArgument(it.hasNext(), "input or output must be part of path.");
        final QName inOrOut = it.next();
        for (final RpcDefinition potential : schema.getOperations()) {
            if (rpcName.equals(potential.getQName())) {
                return SchemaNodeUtils.getRpcDataSchema(potential, inOrOut);
            }
        }
        return null;
    }

    private static SchemaNode findNodeInModule(final Module module, final Iterable<QName> path) {

        Preconditions.checkArgument(module != null, "Parent reference cannot be NULL");
        Preconditions.checkArgument(path != null, "Path reference cannot be NULL");

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

        Preconditions.checkArgument(parent != null, "Parent reference cannot be NULL");
        Preconditions.checkArgument(path != null, "Path reference cannot be NULL");

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

        if (foundNode == null && parent instanceof RpcDefinition) {
            final RpcDefinition parentRpcDefinition = (RpcDefinition) parent;

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
                for (final ChoiceCaseNode caseNode : ((ChoiceSchemaNode) parent).getCases()) {
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

    private static GroupingDefinition getGroupingByName(final RpcDefinition rpc, final QName name) {
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
     *
     * @throws IllegalArgumentException if any arguments are null
     *
     */
    private static List<QName> xpathToQNamePath(final SchemaContext context, final Module parentModule,
            final String xpath) {
        // FIXME: 2.0.0: this should throw NPE, not IAE
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(parentModule != null, "Parent Module reference cannot be NULL");
        Preconditions.checkArgument(xpath != null, "XPath string reference cannot be NULL");

        final List<QName> path = new LinkedList<>();
        for (final String pathComponent : SLASH_SPLITTER.split(xpath)) {
            if (!pathComponent.isEmpty()) {
                path.add(stringPathPartToQName(context, parentModule, pathComponent));
            }
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
     * If Schema Context, Parent Module or Prefixed Path Part refers to
     * <code>null</code> the method will throw IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param parentModule
     *            Parent Module
     * @param prefixedPathPart
     *            Prefixed Path Part string
     * @return QName from prefixed Path Part String.
     * @throws IllegalArgumentException if any arguments are null
     */
    private static QName stringPathPartToQName(final SchemaContext context, final Module parentModule,
            final String prefixedPathPart) {
        // FIXME: 2.0.0: this should throw NPE, not IAE
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(parentModule != null, "Parent Module reference cannot be NULL");
        Preconditions.checkArgument(prefixedPathPart != null, "Prefixed Path Part cannot be NULL!");

        if (prefixedPathPart.indexOf(':') != -1) {
            final Iterator<String> prefixedName = COLON_SPLITTER.split(prefixedPathPart).iterator();
            final String modulePrefix = prefixedName.next();

            final Module module = resolveModuleForPrefix(context, parentModule, modulePrefix);
            Preconditions.checkArgument(module != null,
                    "Failed to resolve xpath: no module found for prefix %s in module %s", modulePrefix,
                    parentModule.getName());

            return QName.create(module.getQNameModule(), prefixedName.next());
        }

        return QName.create(parentModule.getNamespace(), parentModule.getRevision(), prefixedPathPart);
    }

    /**
     * Method will attempt to resolve and provide Module reference for specified
     * module prefix. Each Yang module could contains multiple imports which
     * MUST be associated with corresponding module prefix. The method simply
     * looks into module imports and returns the module that is bounded with
     * specified prefix. If the prefix is not present in module or the prefixed
     * module is not present in specified Schema Context, the method will return
     * <code>null</code>. <br>
     * If String prefix is the same as prefix of the specified Module the
     * reference to this module is returned. <br>
     * If Schema Context, Module or Prefix are referring to <code>null</code>
     * the method will return IllegalArgumentException
     *
     * @param context
     *            Schema Context
     * @param module
     *            Yang Module
     * @param prefix
     *            Module Prefix
     * @return Module for given prefix in specified Schema Context if is
     *         present, otherwise returns <code>null</code>
     * @throws IllegalArgumentException if any arguments are null
     */
    private static Module resolveModuleForPrefix(final SchemaContext context, final Module module,
            final String prefix) {
        // FIXME: 2.0.0: this should throw NPE, not IAE
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(prefix != null, "Prefix string cannot be NULL");

        if (prefix.equals(module.getPrefix())) {
            return module;
        }

        final Set<ModuleImport> imports = module.getImports();
        for (final ModuleImport mi : imports) {
            if (prefix.equals(mi.getPrefix())) {
                return context.findModuleByName(mi.getModuleName(), mi.getRevision());
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
     * @param relativeXPath
     *            Non conditional Revision Aware Relative XPath
     * @param actualSchemaNode
     *            actual schema node
     * @return list of QName
     * @throws IllegalArgumentException if any arguments are null
     */
    private static Iterable<QName> resolveRelativeXPath(final SchemaContext context, final Module module,
            final RevisionAwareXPath relativeXPath, final SchemaNode actualSchemaNode) {
        // FIXME: 2.0.0: this should throw NPE, not IAE
        Preconditions.checkArgument(context != null, "Schema Context reference cannot be NULL");
        Preconditions.checkArgument(module != null, "Module reference cannot be NULL");
        Preconditions.checkArgument(relativeXPath != null, "Non Conditional Revision Aware XPath cannot be NULL");
        Preconditions.checkState(!relativeXPath.isAbsolute(),
                "Revision Aware XPath MUST be relative i.e. MUST contains ../, "
                        + "for non relative Revision Aware XPath use findDataSchemaNode method");
        Preconditions.checkState(actualSchemaNode.getPath() != null,
                "Schema Path reference for Leafref cannot be NULL");

        final Iterable<String> xpaths = SLASH_SPLITTER.split(relativeXPath.toString());

        // Find out how many "parent" components there are
        // FIXME: is .contains() the right check here?
        // FIXME: case ../../node1/node2/../node3/../node4
        int colCount = 0;
        for (final Iterator<String> it = xpaths.iterator(); it.hasNext() && it.next().contains(".."); ) {
            ++colCount;
        }

        final Iterable<QName> schemaNodePath = actualSchemaNode.getPath().getPathFromRoot();

        if (Iterables.size(schemaNodePath) - colCount >= 0) {
            return Iterables.concat(Iterables.limit(schemaNodePath, Iterables.size(schemaNodePath) - colCount),
                Iterables.transform(Iterables.skip(xpaths, colCount),
                    input -> stringPathPartToQName(context, module, input)));
        }
        return Iterables.concat(schemaNodePath,
                Iterables.transform(Iterables.skip(xpaths, colCount),
                    input -> stringPathPartToQName(context, module, input)));
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
        RevisionAwareXPath pathStatement = typeDefinition.getPathStatement();
        pathStatement = new RevisionAwareXPathImpl(stripConditionsFromXPathString(pathStatement),
            pathStatement.isAbsolute());

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

            Module parentModule = findParentModuleOfReferencingType(schemaContext, baseSchema);
            dataSchemaNode = (DataSchemaNode) SchemaContextUtil.findDataSchemaNode(schemaContext, parentModule,
                    pathStatement);
        } else {
            Module parentModule = findParentModule(schemaContext, schema);
            dataSchemaNode = (DataSchemaNode) SchemaContextUtil.findDataSchemaNodeForRelativeXPath(schemaContext,
                    parentModule, schema, pathStatement);
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
        final RevisionAwareXPath pathStatement = typeDefinition.getPathStatement();
        final RevisionAwareXPath strippedPathStatement = new RevisionAwareXPathImpl(
            stripConditionsFromXPathString(pathStatement), pathStatement.isAbsolute());
        if (!strippedPathStatement.isAbsolute()) {
            return null;
        }

        final Module parentModule = schemaContext.findModuleByNamespaceAndRevision(qname.getNamespace(),
                qname.getRevision().orElse(null));
        final DataSchemaNode dataSchemaNode = (DataSchemaNode) SchemaContextUtil.findDataSchemaNode(schemaContext,
            parentModule, strippedPathStatement);
        final TypeDefinition<?> targetTypeDefinition = typeDefinition(dataSchemaNode);
        if (targetTypeDefinition instanceof LeafrefTypeDefinition) {
            return getBaseTypeForLeafRef((LeafrefTypeDefinition) targetTypeDefinition, schemaContext, dataSchemaNode);
        }

        return targetTypeDefinition;
    }

    private static Module findParentModuleOfReferencingType(final SchemaContext schemaContext,
            final SchemaNode schemaNode) {
        Preconditions.checkArgument(schemaContext != null, "Schema Context reference cannot be NULL!");
        Preconditions.checkArgument(schemaNode instanceof TypedSchemaNode, "Unsupported node %s", schemaNode);

        TypeDefinition<?> nodeType = ((TypedSchemaNode) schemaNode).getType();
        if (nodeType.getBaseType() != null) {
            while (nodeType.getBaseType() != null) {
                nodeType = nodeType.getBaseType();
            }

            final QNameModule typeDefModuleQname = nodeType.getQName().getModule();
            return schemaContext.findModuleByNamespaceAndRevision(typeDefModuleQname.getNamespace(),
                    typeDefModuleQname.getRevision().orElse(null));
        }

        return SchemaContextUtil.findParentModule(schemaContext, schemaNode);
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
    static String stripConditionsFromXPathString(final RevisionAwareXPath pathStatement) {
        return STRIP_PATTERN.matcher(pathStatement.toString()).replaceAll("");
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
