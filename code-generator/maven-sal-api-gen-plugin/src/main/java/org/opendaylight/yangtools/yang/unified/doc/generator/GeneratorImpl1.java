/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.unified.doc.generator;

import static org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil.EMPTY_STRING;
import static org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil.NEW_LINE;
import static org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil.concat;
import static org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil.indentWithNewLine;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.unified.doc.generator.util.GeneratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

public class GeneratorImpl1 {

    private File path;
    private static final SimpleDateFormat REVISION_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger LOG = LoggerFactory.getLogger(GeneratorImpl.class);
    private static final BuildContext CTX = new DefaultBuildContext();
    private Module currentModule;
    private final Map<String, String> imports = new HashMap<>();
    private SchemaContext ctx;

    protected Set<File> generate(final SchemaContext context, final File targetPath, final Set<Module> modulesToGen)
            throws IOException {
        this.path = targetPath;
        this.path.mkdirs();
        final Set<File> genDocFiles = new HashSet<>();

        for (final Module module : modulesToGen) {
            genDocFiles.add(generateDocumentation(module, context));
        }
        return genDocFiles;
    }

    private File generateDocumentation(final Module module, final SchemaContext ctx) {
        final File destination = new File(path, module.getName() + ".html");
        this.ctx = ctx;

        for (final ModuleImport importModule : module.getImports()) {
            imports.put(importModule.getPrefix(), importModule.getModuleName());
        }

        try {
            final OutputStreamWriter fw = new OutputStreamWriter(CTX.newFileOutputStream(destination));
            final BufferedWriter bw = new BufferedWriter(fw);
            currentModule = module;
            bw.append(generate(module, ctx));
            bw.close();
            fw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return destination;
    }

    private String generate(final Module module, final SchemaContext schemaContext) {
        final StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>");
        sb.append("<html lang=\"en\">");
        sb.append(indentWithNewLine(1, "<head>"));
        sb.append(indentWithNewLine(2, concat("<title>", module.getName(), "</title>")));
        sb.append(indentWithNewLine(1, "</head>"));
        sb.append(indentWithNewLine(1, "<body>"));
        sb.append(body(2, module, ctx));
        sb.append(indentWithNewLine(1, "</body>"));
        sb.append("</html>");

        return sb.toString();
    }

    private String body(final int numOfTab, final Module module, final SchemaContext schemaContext) {
        final StringBuilder sb = new StringBuilder();

        sb.append(header(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(typeDefinitionsSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(identitiesSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(groupingsSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(augmentationsSummary(numOfTab, module, schemaContext));
        sb.append(NEW_LINE);
        sb.append(objectsSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(notificationsSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(rpcsSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(extensionsSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(featuresSummary(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(typeDefinitions(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(identities(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(groupings(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(dataStore(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(childNodes(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(notifications(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(augmentations(numOfTab, module, ctx));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(rpcs(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(extensions(numOfTab, module));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(features(numOfTab, module));

        return sb.toString();
    }

    private String features(final int numOfTab, final Module module) {
        if (module.getFeatures().isEmpty()) {
            return EMPTY_STRING;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, "<h2>Features</h2>"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfTab, "<ul>"));

        for (final FeatureDefinition feature : module.getFeatures()) {
            sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
            sb.append(indentWithNewLine(numOfTab + 2, concat("<h3 id=\"", feature.getQName().getLocalName(), "\"</h3>")));
            sb.append(indentWithNewLine(numOfTab + 2, "<ul>"));
            sb.append(descAndRefLi(numOfTab + 3, feature));
            sb.append(indentWithNewLine(numOfTab + 2, "</ul>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
        }
        sb.append(indentWithNewLine(numOfTab, "</ul>"));
        return sb.toString();
    }

    private String extensions(final int numOfTab, final Module module) {
        if (module.getExtensionSchemaNodes().isEmpty()) {
            return EMPTY_STRING;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, "<h2>Extensions</h2>"));

        for (final ExtensionDefinition extensionDef : module.getExtensionSchemaNodes()) {
            sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
            sb.append(indentWithNewLine(numOfTab + 2, concat("<h3 id=\"", extensionDef.getQName().getLocalName(), "\">", nodeName(extensionDef), "</h3>")));
            sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
            sb.append(extensionInfo(numOfTab + 1, extensionDef));
        }
        return sb.toString();
    }

    private String extensionInfo(final int numOfTab, final ExtensionDefinition extension) {
        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(descAndRefLi(numOfTab + 1, extension));
        sb.append(listItem(numOfTab + 1, "Argument", extension.getArgument()));
        sb.append(indentWithNewLine(numOfTab, "</ul>"));

        return sb.toString();
    }

    private String rpcs(final int numOfTab, final Module module) {
        if (module.getRpcs().isEmpty()) {
            return EMPTY_STRING;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, "<h2>RPC Definitions</h2>"));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));

        for (RpcDefinition rpc : module.getRpcs()) {
            sb.append(indentWithNewLine(numOfTab + 1, concat("<h3 id=\"", rpc.getQName().getLocalName(), "\">", nodeName(rpc), "</h3>")));
            sb.append(indentWithNewLine(numOfTab + 2, "<ul>"));
            sb.append(descAndRefLi(numOfTab + 3, rpc));
            sb.append(printSchemaNodeInfo(numOfTab + 3, rpc.getInput()));
            sb.append(printSchemaNodeInfo(numOfTab + 3, rpc.getOutput()));
            sb.append(indentWithNewLine(numOfTab + 2, "</ul>"));
        }
        sb.append(indentWithNewLine(numOfTab, "</ul>"));

        return sb.toString();
    }

    private String augmentations(final int numOfTab, final Module module, final SchemaContext context) {
        final StringBuilder sb = new StringBuilder();

        if (module.getAugmentations().isEmpty()) {
            return EMPTY_STRING;
        }
        sb.append(indentWithNewLine(numOfTab, "<h2>Augmentations</h2"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfTab, "<ul>"));

        for (final AugmentationSchema augment : module.getAugmentations()) {
            sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
            sb.append(indentWithNewLine(numOfTab + 2, concat("<h3 id=\"", schemaPathToString(module, augment.getTargetPath(), context, augment))));
            sb.append(indentWithNewLine(numOfTab + 2, concat("Target [", typeAnchorLink(augment.getTargetPath(), schemaPathToString(module, augment.getTargetPath(), context, augment)), "]</h3>")));
            sb.append(indentWithNewLine(numOfTab + 2, augment.getDescription()));
            sb.append(indentWithNewLine(numOfTab + 3, concat("Status: ", strong(String.valueOf(augment.getStatus())))));

            if (augment.getReference() != null) {
                sb.append(indentWithNewLine(numOfTab + 3, concat("Reference: ", augment.getReference())));
            }
            if (augment.getWhenCondition() != null) {
                sb.append(indentWithNewLine(numOfTab + 3, concat("When ", augment.getWhenCondition().toString())));
            }
            for (final DataSchemaNode childNode : augment.getChildNodes()) {
                sb.append(printSchemaNodeInfo(numOfTab + 3, childNode));
            }
            sb.append(NEW_LINE);
            sb.append(indentWithNewLine(numOfTab + 3, "<h3>Example</h3>"));
            sb.append(createAugmentChildNodesAsString(numOfTab + 3, new ArrayList<DataSchemaNode>(augment.getChildNodes())));
            sb.append(printNodeChildren(numOfTab + 3, parseTargetPath(augment.getTargetPath())));
        }

        return null;
    }

    private String printNodeChildren(final int numOfTab, final List<DataSchemaNode> childNodes) {
        if (childNodes.isEmpty()) {
            return EMPTY_STRING;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, "<pre>"));
        sb.append(printAugmentedNode(numOfTab + 1, childNodes.get(0)));
        sb.append(indentWithNewLine(numOfTab, "</pre>"));

        return sb.toString();
    }

    private List<DataSchemaNode> parseTargetPath(SchemaPath path) {
        final List<DataSchemaNode> nodes = new ArrayList<>();
        DataSchemaNode lastNodeInTargetPath = null;

        for (final QName pathElement : path.getPathFromRoot()) {
            final Module module = ctx.findModuleByNamespaceAndRevision(pathElement.getNamespace(), pathElement.getRevision());
            if (module != null) {
                DataSchemaNode foundNode = module.getDataChildByName(pathElement);
                if (foundNode == null) {
                    final DataSchemaNode child = Iterables.getLast(nodes);
                    if (child instanceof DataNodeContainer) {
                        final DataNodeContainer dataContNode = (DataNodeContainer)child;
                        foundNode = findNodeInChildNodes(pathElement, dataContNode.getChildNodes());
                    }
                }
                if (foundNode != null) {
                    nodes.add(foundNode);
                }
            }
        }
        if (!nodes.isEmpty()) {
            lastNodeInTargetPath = nodes.get(nodes.size() - 1);
        }

        final List<DataSchemaNode> targetPathNodes = new ArrayList<>();
        targetPathNodes.add(lastNodeInTargetPath);

        return targetPathNodes;
    }

    private DataSchemaNode findNodeInChildNodes(final QName findingNode, final Iterable<DataSchemaNode> childNodes) {
        DataSchemaNode foundChild = null;

        for (final DataSchemaNode child : childNodes) {
            if (child.getQName().equals(findingNode)) {
                return child;
            }
        }

        // find recursively
        for (final DataSchemaNode child : childNodes) {
            if (child instanceof ContainerSchemaNode) {
                final ContainerSchemaNode contNode = (ContainerSchemaNode)child;
                foundChild = findNodeInChildNodes(findingNode, contNode.getChildNodes());
                if (foundChild != null) {
                    return foundChild;
                }
            }
            else if (child instanceof ListSchemaNode) {
                final ListSchemaNode listNode = (ListSchemaNode)child;
                foundChild = findNodeInChildNodes(findingNode, listNode.getChildNodes());
                if (foundChild != null) {
                    return foundChild;
                }
            }
        }
        return foundChild;
    }

    private String createAugmentChildNodesAsString(final int numOfTab, final ArrayList<DataSchemaNode> childNodes) {
        final StringBuilder augmentChildNodesAsString = new StringBuilder();
        augmentChildNodesAsString.append(printNodeChildren(numOfTab, childNodes));
        return augmentChildNodesAsString.toString();
    }

    private String printNodeChildren(final int numOfTab, final ArrayList<DataSchemaNode> childNodes) {
        final StringBuilder sb = new StringBuilder();

        if (childNodes.isEmpty()) {
            return EMPTY_STRING;
        }

        sb.append(indentWithNewLine(numOfTab, "<pre>"));
        for (final DataSchemaNode childNode : childNodes) {
            sb.append(printAugmentedNode(numOfTab, childNode));
        }

        sb.append(indentWithNewLine(numOfTab, "</pre>"));
        return sb.toString();
    }

    private String printAugmentedNode(final int numOfTab, final DataSchemaNode childNode) {
        final StringBuilder sb = new StringBuilder();

        if (childNode instanceof ChoiceCaseNode) {
            return EMPTY_STRING;
        }

        if (childNode instanceof ContainerSchemaNode) {
            sb.append(printContainerNode(numOfTab, (ContainerSchemaNode)childNode));
        }
        else if (childNode instanceof AnyXmlSchemaNode) {
            sb.append(printAnyXmlNode(numOfTab, (AnyXmlSchemaNode)childNode));
        }
        else if (childNode instanceof LeafSchemaNode) {
            sb.append(printLeafNode(numOfTab, (LeafSchemaNode)childNode));
        }
        else if (childNode instanceof LeafListSchemaNode) {
            sb.append(printLeafListNode(numOfTab, (LeafListSchemaNode)childNode));
        }
        else if (childNode instanceof ListSchemaNode) {
            sb.append(printListNode(numOfTab, (ListSchemaNode)childNode));
        }
        else if (childNode instanceof ChoiceNode) {
            sb.append(printChoiceNode(numOfTab, (ChoiceNode)childNode));
        }
        return sb.toString();
    }

    private String printChoiceNode(final int numOfTab, final ChoiceNode choiceNode) {
        final StringBuilder sb = new StringBuilder();
        final Set<ChoiceCaseNode> cases = choiceNode.getCases();

        if (cases.isEmpty()) {
            return EMPTY_STRING;
        } else {
            for (final ChoiceCaseNode caseNode : cases) {
                sb.append(printAugmentedNode(numOfTab + 1, caseNode));
            }
        }

        return sb.toString();
    }

    private String printListNode(final int numOfTab, final ListSchemaNode listNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, concat("&lt;", listNode.getQName().getLocalName())));

        if (!listNode.getQName().getNamespace().equals(currentModule.getNamespace())) {
            sb.append(concat(" xmlns=\"", listNode.getQName().getNamespace().toString(), "\""));
        }
        sb.append("&gt;");

        for (final DataSchemaNode childNode : listNode.getChildNodes()) {
            sb.append(printAugmentedNode(numOfTab + 1, childNode));
        }
        sb.append(indentWithNewLine(numOfTab, concat("&gt;", listNode.getQName().getLocalName(), "&lt;")));

        return sb.toString();
    }

    private String printLeafListNode(final int numOfTab, final LeafListSchemaNode leafListNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, concat("&lt;", leafListNode.getQName().getLocalName(), "&gt;. . .&lt;/", leafListNode.getQName().getLocalName(), "&gt;")));
        sb.append(indentWithNewLine(numOfTab, concat("&lt;", leafListNode.getQName().getLocalName(), "&gt;. . .&lt;/", leafListNode.getQName().getLocalName(), "&gt;")));
        sb.append(indentWithNewLine(numOfTab, concat("&lt;", leafListNode.getQName().getLocalName(), "&gt;. . .&lt;/", leafListNode.getQName().getLocalName(), "&gt;")));

        return sb.toString();
    }

    private String printLeafNode(final int numOfTab, final LeafSchemaNode leafNode) {
        return indentWithNewLine(numOfTab, concat("&gt;", leafNode.getQName().getLocalName(), "&gt;. . .&lt;/", leafNode.getQName().getLocalName(), "&gt;"));
    }

    private String printAnyXmlNode(final int numOfTab, final AnyXmlSchemaNode anyXmlNode) {
        return indentWithNewLine(numOfTab, concat("&gt;", anyXmlNode.getQName().getLocalName(), "&gt;. . .&lt;/", anyXmlNode.getQName().getLocalName(), "&gt;"));
    }

    private String printContainerNode(final int numOfTab, final ContainerSchemaNode containerNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, concat("&lt;", containerNode.getQName().getLocalName())));

        if (!containerNode.getQName().getNamespace().equals(currentModule.getNamespace())) {
            sb.append(concat(" xmlns=\"", containerNode.getQName().getNamespace().toString(), "\""));
        }
        sb.append("&gt;");

        for (final DataSchemaNode childNode : containerNode.getChildNodes()) {
            sb.append(printAugmentedNode(numOfTab + 1, childNode));
        }
        sb.append(indentWithNewLine(numOfTab, concat("&gt;", containerNode.getQName().getLocalName(), "&lt;")));

        return sb.toString();
    }

    private Object notifications(int numOfTab, Module module) {
        final Set<NotificationDefinition> notificationDefs = module.getNotifications();
        final StringBuilder sb = new StringBuilder();

        if (notificationDefs.isEmpty()) {
            return EMPTY_STRING;
        }

        sb.append(indentWithNewLine(numOfTab, "<h2>Notifications</h2>"));

        for (final NotificationDefinition notification : notificationDefs) {
            sb.append(indentWithNewLine(numOfTab, concat("<h3 id=\"", schemaPathToId(notification.getPath()), "\">", nodeName(notification), "</h3>")));
            sb.append(descAndRefLi(numOfTab + 1, notification));
            for (final DataSchemaNode childNode : notification.getChildNodes()) {
                sb.append(printSchemaNodeInfo(numOfTab + 1, childNode));
            }
        }
        return sb.toString();
    }

    private Object childNodes(int numOfTab, Module module) {
        final StringBuilder sb = new StringBuilder();
        final Collection<DataSchemaNode> childNodes = module.getChildNodes();

        if (!isNullOrEmpty(childNodes)) {
            sb.append(indentWithNewLine(numOfTab, "<h2>Child nodes</h2>"));
            sb.append(NEW_LINE);
            sb.append(printChildren(numOfTab, childNodes, 3, YangInstanceIdentifier.builder().toInstance()));
        }

        return sb.toString();
    }

    private String printChildren(final int numOfTab, final Iterable<DataSchemaNode> nodes, final int level, final YangInstanceIdentifier path) {
        final StringBuilder sb = new StringBuilder();
        final Iterable<AnyXmlSchemaNode> anyxmlNodes = GeneratorUtil.filter(nodes, AnyXmlSchemaNode.class);
        final Iterable<LeafSchemaNode> leafNodes = GeneratorUtil.filter(nodes, LeafSchemaNode.class);
        final Iterable<LeafListSchemaNode> leafListNodes = GeneratorUtil.filter(nodes, LeafListSchemaNode.class);
        final Iterable<ChoiceNode> choices = GeneratorUtil.filter(nodes, ChoiceNode.class);
        final Iterable<ChoiceCaseNode> cases = GeneratorUtil.filter(nodes, ChoiceCaseNode.class);
        final Iterable<ContainerSchemaNode> containers = GeneratorUtil.filter(nodes, ContainerSchemaNode.class);
        final Iterable<ListSchemaNode> lists = GeneratorUtil.filter(nodes, ListSchemaNode.class);

        if (Iterables.size(anyxmlNodes) + Iterables.size(leafNodes) + Iterables.size(leafListNodes) + Iterables.size(containers) + Iterables.size(lists) > 0) {
            sb.append(indentWithNewLine(numOfTab, "<h3>Direct children</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<ul>"));
        }
        for (final AnyXmlSchemaNode anyxmlNode : anyxmlNodes) {
            sb.append(printShortInfo(numOfTab + 1, anyxmlNode, level, path));
        }
        for (final LeafSchemaNode leafSchemaNode : leafNodes) {
            sb.append(printShortInfo(numOfTab + 1, leafSchemaNode, level, path));
        }
        for (final LeafListSchemaNode leafListSchemaNode : leafListNodes) {
            sb.append(printShortInfo(numOfTab + 1, leafListSchemaNode, level, path));
        }
        for (final ContainerSchemaNode container : containers) {
            sb.append(printShortInfo(numOfTab + 1, container, level, path));
        }
        for (final ListSchemaNode listSchemaNode : lists) {
            sb.append(printShortInfo(numOfTab + 1, listSchemaNode, level, path));
        }

        sb.append(NEW_LINE);

        if (path.getPathArguments().iterator().hasNext()) {
            sb.append(xmlExample(numOfTab + 1, nodes, Iterables.getLast(path.getPathArguments()).getNodeType(), path));
        }

        for (final ContainerSchemaNode container : containers) {
            sb.append(printInfo(numOfTab + 1, container, level, path));
        }
        for (final ListSchemaNode listSchemaNode : lists) {
            sb.append(printInfo(numOfTab + 1, listSchemaNode, level, path));
        }
        for (final ChoiceNode choice : choices) {
            sb.append(printInfo(numOfTab + 1, choice, level, path));
        }
        for (final ChoiceCaseNode caseNode : cases) {
            sb.append(printInfo(numOfTab + 1, caseNode, level, path));
        }

        return EMPTY_STRING;
    }

    private String printInfo(final int numOfTab, final ChoiceCaseNode caseNode, final int level, final YangInstanceIdentifier path) {
        final StringBuilder sb = new StringBuilder();
        sb.append(printChildren(numOfTab, caseNode.getChildNodes(), level, path));

        return sb.toString();
    }

    private String printInfo(final int numOfTab, final ChoiceNode choice, final int level, final YangInstanceIdentifier path) {
        final Set<DataSchemaNode> choiceCases = Sets.<DataSchemaNode>newHashSet(choice.getCases());
        final StringBuilder sb = new StringBuilder();
        sb.append(printChildren(numOfTab, choiceCases, level, path));

        return sb.toString();
    }

    private String printInfo(final int numOfTab, final ListSchemaNode listSchemaNode, final int level, final YangInstanceIdentifier path) {
        final YangInstanceIdentifier newPath = append(path, listSchemaNode);
        final StringBuilder sb = new StringBuilder();

        sb.append(header(numOfTab, level, newPath));
        sb.append(indentWithNewLine(numOfTab, "<dl>"));
        sb.append(indentWithNewLine(numOfTab + 1, "<dt>XML Path</dt>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<dd>", asXmlPath(newPath), "</dd>")));
        sb.append(indentWithNewLine(numOfTab + 1, "<dt>Restconf path</dt>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<dd>", code(asRestconfPath(newPath)), "</dd>")));
        sb.append(indentWithNewLine(numOfTab, "</dl>"));
        sb.append(printChildren(numOfTab, listSchemaNode.getChildNodes(), level, newPath));

        return sb.toString();
    }

    private String code(String text) {
        return concat("<code>", text, "</code>");
    }

    private String asRestconfPath(final YangInstanceIdentifier identifier) {
        final StringBuilder sb = new StringBuilder();
        boolean previous = false;

        sb.append(concat(currentModule.getName(), ":"));

        for (final PathArgument pathArg : identifier.getPathArguments()) {
            if (previous) {
                sb.append("/");
            }
            sb.append(pathArg.getNodeType().getLocalName());
            previous = true;

            if (pathArg instanceof NodeIdentifierWithPredicates) {
                final NodeIdentifierWithPredicates nodeIdentifier = (NodeIdentifierWithPredicates)pathArg;

                for (final QName qname : nodeIdentifier.getKeyValues().keySet()) {
                    sb.append(concat("/{", qname.getLocalName(), "}"));
                }
            }
        }
        return sb.toString();
    }

    private String asXmlPath(final YangInstanceIdentifier newPath) {
        return EMPTY_STRING;
    }

    private YangInstanceIdentifier append(final YangInstanceIdentifier identifier, final ContainerSchemaNode node) {
        return identifier.node(node.getQName());
    }

    private YangInstanceIdentifier append(final YangInstanceIdentifier identifier, final ListSchemaNode node) {
        final Map<QName, Object> keyValues = Maps.<QName, Object>newLinkedHashMap();

        if (node.getKeyDefinition() != null) {
            for (final QName key : node.getKeyDefinition()) {
                keyValues.put(key, new Object());
            }
        }
        return identifier.node(new NodeIdentifierWithPredicates(node.getQName(), keyValues));
    }

    private String localLink(final YangInstanceIdentifier identifier, final String localName) {
        final StringBuilder sb = new StringBuilder();

        sb.append("<a href=\"#");
        for (PathArgument pathArgument : identifier.getPathArguments()) {
            sb.append(pathArgument.getNodeType().getLocalName());
            sb.append("/");
        }
        sb.append(concat("\">", localName, "</a>"));
        return sb.toString();
    }

    private String printInfo(final int numOfTab, final ContainerSchemaNode container, final int level, final YangInstanceIdentifier path) {
        final YangInstanceIdentifier newPath = append(path, container);
        final StringBuilder sb = new StringBuilder();

        sb.append(header(numOfTab, level, newPath));
        sb.append(indentWithNewLine(numOfTab, "<dl>"));
        sb.append(indentWithNewLine(numOfTab + 1, "<dt>XML Path</dt>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<dd>", asXmlPath(newPath), "</dd>")));
        sb.append(indentWithNewLine(numOfTab + 1, "<dt>Restconf path</dt>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<dd>", code(asRestconfPath(newPath)), "</dd>")));
        sb.append(indentWithNewLine(numOfTab, "</dl>"));
        sb.append(printChildren(numOfTab, container.getChildNodes(), level, newPath));

        return sb.toString();
    }

    private String printShortInfo(final int numOfTab, final AnyXmlSchemaNode anyxmlNode, final int level, final YangInstanceIdentifier path) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, concat("<li>", strong(anyxmlNode.getQName().getLocalName()), " (anyxml)")));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<li>configuration data: ", strong(String.valueOf(anyxmlNode.isConfiguration())), "</li>")));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<li>mandatory: ", strong(String.valueOf(anyxmlNode.getConstraints().isMandatory())), "</li>")));
        sb.append(indentWithNewLine(numOfTab, "</ul>"));
        sb.append(indentWithNewLine(numOfTab, "</li>"));

        return sb.toString();
    }

    private String printShortInfo(final int numOfTab, final LeafSchemaNode leafSchemaNode, final int level, final YangInstanceIdentifier path) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, concat("<li>", strong(leafSchemaNode.getQName().getLocalName()), " (leaf)")));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<li>configuration data: ", strong(String.valueOf(leafSchemaNode.isConfiguration())), "</li>")));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<li>mandatory: ", strong(String.valueOf(leafSchemaNode.getConstraints().isMandatory())), "</li>")));
        sb.append(indentWithNewLine(numOfTab, "</ul>"));
        sb.append(indentWithNewLine(numOfTab, "</li>"));

        return sb.toString();
    }

    private String printShortInfo(final int numOfTab, final LeafListSchemaNode leafListSchemaNode, final int level, final YangInstanceIdentifier path) {
        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, concat("<li>", strong(leafListSchemaNode.getQName().getLocalName()), " (leaf-list)")));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<li>configuration data: ", strong(String.valueOf(leafListSchemaNode.isConfiguration())), "</li>")));
        sb.append(indentWithNewLine(numOfTab, "</ul>"));
        sb.append(indentWithNewLine(numOfTab, "</li>"));

        return sb.toString();
    }

    private String printShortInfo(final int numOfTab, final ListSchemaNode listSchemaNode, final int level, final YangInstanceIdentifier path) {
        final YangInstanceIdentifier newPath = append(path, listSchemaNode);
        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, concat("<li>", strong(localLink(newPath, listSchemaNode.getQName().getLocalName())), " (list)")));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<li>configuration data: ", strong(String.valueOf(listSchemaNode.isConfiguration())), "</li>")));
        sb.append(indentWithNewLine(numOfTab, "</ul>"));
        sb.append(indentWithNewLine(numOfTab, "</li>"));

        return sb.toString();
    }

    private String printShortInfo(final int numOfTab, final ContainerSchemaNode container, final int level, final YangInstanceIdentifier path) {
        final YangInstanceIdentifier newPath = append(path, container);
        final StringBuilder sb = new StringBuilder();
        sb.append(indentWithNewLine(numOfTab, concat("<li>", strong(localLink(newPath, container.getQName().getLocalName())), " (container)")));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(indentWithNewLine(numOfTab + 1, concat("<li>configuration data: ", strong(String.valueOf(container.isConfiguration())), "</li>")));
        sb.append(indentWithNewLine(numOfTab, "</ul>"));
        sb.append(indentWithNewLine(numOfTab, "</li>"));

        return sb.toString();
    }

    private String xmlExample(final int numOfTab, final Iterable<DataSchemaNode> nodes, final QName name, final YangInstanceIdentifier path) {
        final StringBuilder sb = new StringBuilder();
        final String childNodesAsString = xmplExampleTags(numOfTab, nodes, path);

        sb.append(indentWithNewLine(numOfTab, "<pre>"));
        sb.append(childNodesAsString);
        sb.append(indentWithNewLine(numOfTab, "</pre>"));

        return sb.toString();
    }

    private String xmplExampleTags(final int numOfTab, final Iterable<DataSchemaNode> nodes, final YangInstanceIdentifier identifier) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, "<!-- Child nodes -->"));
        for (final DataSchemaNode node : nodes) {
            sb.append(indentWithNewLine(numOfTab, concat("<!-- ", node.getQName().getLocalName(), " -->")));
            sb.append(indentWithNewLine(numOfTab + 1, asXmlExampleTag(node, identifier)));
        }
        return sb.toString();
    }

    private String asXmlExampleTag(final DataSchemaNode node, final YangInstanceIdentifier identifier) {
        return "<!-- noop -->";
    }



    private boolean isNullOrEmpty(final Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    private String dataStore(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();

        if (module.getChildNodes().isEmpty()) {
            return EMPTY_STRING;
        }

        sb.append(indentWithNewLine(numOfTab, "<h2>Datastore Structure</h2>"));
        sb.append(tree(numOfTab, module));

        return sb.toString();
    }

    private String tree(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, strong(module.getName())));
        sb.append(treeSet(numOfTab, module.getChildNodes(), YangInstanceIdentifier.builder().toInstance()));

        return sb.toString();
    }

    private String treeSet(final int numOfTab, final Collection<DataSchemaNode> childNodes, final YangInstanceIdentifier path) {
        final StringBuilder sb = new StringBuilder();

        if (childNodes != null && !childNodes.isEmpty()) {
            sb.append(indentWithNewLine(numOfTab, "<ul>"));

            for (final DataSchemaNode child : childNodes) {
                sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
                sb.append(tree(numOfTab, child, path));
                sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
            }
            sb.append(indentWithNewLine(numOfTab, "</ul>"));
        }
        return sb.toString();
    }

    private String tree(final int numOfTab, final DataSchemaNode node, final YangInstanceIdentifier path) {
        return nodeName(numOfTab, node);
    }

    private String nodeName(final int numOfTab, final DataSchemaNode node) {
        final StringBuilder sb = new StringBuilder();

        if (node.isAugmenting()) {
            sb.append("(A)");
        }
        if (node.isAddedByUses()) {
            sb.append("(U)");
        }
        return sb.toString();
    }

    private String addedByInfo(final SchemaNode schemaNode) {
        return EMPTY_STRING;
    }

    private boolean isAddedBy(final DataSchemaNode node) {
        if (node.isAugmenting() || node.isAddedByUses()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isAddedBy(final SchemaNode node) {
        return false;
    }

    private String nodeName(final SchemaNode node) {
        if (isAddedBy(node)) {
            return concat(italic(node.getQName().getLocalName()), addedByInfo(node));
        } else {
            return concat(node.getQName().getLocalName(), addedByInfo(node));
        }
    }

    private String nodeName(final ContainerSchemaNode node) {
        if (node.isAddedByUses()) {
            return concat(strong(italic(node.getQName().getLocalName())), addedByInfo(node));
        } else {
            return concat(strong(node.getQName().getLocalName()), addedByInfo(node));
        }
    }

    private String italic(final String text) {
        return concat("<i>", text, "</i>");
    }

    private String nodeName(final ListSchemaNode node) {
        final StringBuilder sb = new StringBuilder();

        if (isAddedBy(node)) {
            sb.append(strong(italic(node.getQName().getLocalName())));
            sb.append(" ");

            if (node.getKeyDefinition() != null && !node.getKeyDefinition().isEmpty()) {
                sb.append(listkeys(node));
            }
            sb.append(addedByInfo(node));
        } else {
            sb.append(strong(node.getQName().getLocalName()));
            sb.append(" ");

            if (node.getKeyDefinition() != null && !node.getKeyDefinition().isEmpty()) {
                sb.append(listkeys(node));
            }
        }
        return sb.toString();
    }

    private Object listkeys(ListSchemaNode node) {
        final StringBuilder sb = new StringBuilder();

        for (final QName key : node.getKeyDefinition()) {
            sb.append(concat(key.getLocalName(), " "));
        }
        return sb.toString();
    }

    private String groupings(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<GroupingDefinition> groupings = module.getGroupings();

        if (groupings.isEmpty()) {
            return EMPTY_STRING;
        }

        sb.append(indentWithNewLine(numOfTab, "<h2>Groupings</h2>"));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));

        for (final GroupingDefinition grouping : groupings) {
            sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
            sb.append(indentWithNewLine(numOfTab + 2, concat("<h3 id=\">", grouping.getQName().getLocalName(), "\">", grouping.getQName().getLocalName(), "</h3>")));
            sb.append(indentWithNewLine(numOfTab + 2, "<ul>"));
            sb.append(descAndRefLi(numOfTab + 2, grouping));

            for (DataSchemaNode childNode : grouping.getChildNodes()) {
                sb.append(printSchemaNodeInfo(numOfTab + 3, childNode));
            }

            sb.append(indentWithNewLine(numOfTab + 2, "</ul>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
        }
        sb.append(indentWithNewLine(numOfTab, "</ul>"));

        return sb.toString();
    }

    private String printSchemaNodeInfo(final int numOfTab, final DataSchemaNode node) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(printBaseInfo(numOfTab, node));

        if (node instanceof DataNodeContainer) {
            final DataNodeContainer dataNode = (DataNodeContainer)node;

            sb.append(indentWithNewLine(numOfTab + 1, "<ul>"));
            for (final UsesNode usesNode : dataNode.getUses()) {
                sb.append(printUses(numOfTab + 2, usesNode));
            }
            sb.append(indentWithNewLine(numOfTab + 1, "</ul>"));

            sb.append(indentWithNewLine(numOfTab + 1, "<ul>"));
            for (final TypeDefinition<?> typeDefinition : dataNode.getTypeDefinitions()) {
                sb.append(restrictions(numOfTab + 2, typeDefinition));
            }
            sb.append(indentWithNewLine(numOfTab + 1, "</ul>"));

            sb.append(indentWithNewLine(numOfTab + 1, "<ul>"));
            for (final GroupingDefinition grouping : dataNode.getGroupings()) {
                sb.append(printGrouping(numOfTab + 2, grouping));
            }
            sb.append(indentWithNewLine(numOfTab + 1, "</ul>"));

            sb.append(indentWithNewLine(numOfTab + 1, "<ul>"));
            for (final DataSchemaNode childNode : dataNode.getChildNodes()) {
                sb.append(printSchemaNodeInfo(numOfTab + 2, childNode));
            }
            sb.append(indentWithNewLine(numOfTab + 1, "</ul>"));
        }

        sb.append(indentWithNewLine(numOfTab, "</ul>"));

        return sb.toString();
    }

    private String printGrouping(final int numOfTab, final GroupingDefinition grouping) {
        return strong(listItem(numOfTab, "grouping", grouping.getQName().getLocalName()));
    }

    private String printUses(final int numOfTab, final UsesNode usesNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(strong(listItem(numOfTab, "uses", typeAnchorLink(usesNode.getGroupingPath(), usesNode.getGroupingPath().getPathTowardsRoot().iterator().next().getLocalName()))));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(indentWithNewLine(numOfTab, "<li>refines:"));
        sb.append(indentWithNewLine(numOfTab + 1, "<ul>"));

        for (final SchemaPath sp : usesNode.getRefines().keySet()) {
            sb.append(listItem(numOfTab + 1, "node-name", usesNode.getRefines().get(sp).getQName().getLocalName()));
        }

        sb.append(indentWithNewLine(numOfTab + 1, "</ul>"));
        sb.append(indentWithNewLine(numOfTab, "</li>"));
        for (final AugmentationSchema augment : usesNode.getAugmentations()) {
            sb.append(indentWithNewLine(numOfTab, typeAnchorLink(augment.getTargetPath(), schemaPathToString(currentModule, augment.getTargetPath(), ctx, augment))));
        }
        sb.append(indentWithNewLine(numOfTab, "</ul>"));

        return sb.toString();
    }

    private String printBaseInfo(final int numOfTab, final DataSchemaNode node) {
        final StringBuilder sb = new StringBuilder();

        if (node instanceof LeafSchemaNode) {
            final LeafSchemaNode leafNode = (LeafSchemaNode)node;

            sb.append(printInfo(numOfTab, leafNode, "leaf"));
            sb.append(listItem(numOfTab, "type", typeAnchorLink(leafNode.getType().getPath(), leafNode.getType().getQName().getLocalName())));
            sb.append(listItem(numOfTab, "units", leafNode.getUnits()));
            sb.append(listItem(numOfTab, "default", leafNode.getDefault()));
            sb.append(indentWithNewLine(numOfTab, "</ul>"));

        } else if (node instanceof LeafListSchemaNode) {
            final LeafListSchemaNode leafListNode = (LeafListSchemaNode)node;

            sb.append(printInfo(numOfTab, node, "leaf-list"));
            sb.append(listItem(numOfTab, "type", leafListNode.getType().getQName().getLocalName()));
            sb.append(indentWithNewLine(numOfTab, "</ul>"));

        } else if (node instanceof ListSchemaNode) {
            final ListSchemaNode listNode = (ListSchemaNode)node;

            sb.append(printInfo(numOfTab, node, "list"));

            for (final QName keyDef : listNode.getKeyDefinition()) {
                sb.append(listItem(numOfTab, "key definition", keyDef.getLocalName()));
            }
            sb.append(indentWithNewLine(numOfTab, "</ul>"));

        } else if (node instanceof ChoiceNode) {
            final ChoiceNode choiceNode = (ChoiceNode)node;

            sb.append(printInfo(numOfTab, node, "choice"));
            sb.append(listItem(numOfTab, "default case", choiceNode.getDefaultCase()));

            for (final ChoiceCaseNode caseNode : choiceNode.getCases()) {
                sb.append(printSchemaNodeInfo(numOfTab, caseNode));
            }
            sb.append(indentWithNewLine(numOfTab, "</ul>"));

        } else if (node instanceof ChoiceCaseNode) {
            sb.append(printInfo(numOfTab, node, "case"));
            sb.append(indentWithNewLine(numOfTab, "</ul>"));

        } else if (node instanceof ContainerSchemaNode) {
            sb.append(printInfo(numOfTab, node, "container"));
            sb.append(indentWithNewLine(numOfTab, "</ul>"));

        } else if (node instanceof AnyXmlSchemaNode) {
            sb.append(printInfo(numOfTab, node, "anyxml"));
            sb.append(indentWithNewLine(numOfTab, "</ul>"));
        }
        return sb.toString();
    }

    private String printInfo(final int numOfTab, final SchemaNode node, final String description) {
        final StringBuilder sb = new StringBuilder();
        final ConstraintDefinition constraintDef = ((DataSchemaNode)node).getConstraints();

        if (node instanceof AugmentationTarget) {
            if (node != null) {
                sb.append(indentWithNewLine(numOfTab + 1, "<strong>"));
                sb.append(indentWithNewLine(numOfTab + 1, concat("<li id=\"", schemaPathToId(node.getPath()), "\">")));
                sb.append(indentWithNewLine(numOfTab + 2, concat(description, ": ", node.getQName().getLocalName())));
                sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</strong>"));
            }
        } else {
            sb.append(strong(listItem(numOfTab + 1, description, node.getQName().getLocalName())));
        }
        sb.append(indentWithNewLine(numOfTab, "<ul>"));
        sb.append(listItem(numOfTab + 1, "description", node.getDescription()));
        sb.append(listItem(numOfTab + 1, "reference", node.getReference()));

        if (node instanceof DataSchemaNode && (constraintDef != null)) {
            sb.append(listItem(numOfTab + 1, "when condition", constraintDef.getWhenCondition() != null ? constraintDef.getWhenCondition().toString() : EMPTY_STRING));
            sb.append(listItem(numOfTab + 1, "min elements", constraintDef.getMinElements() != null ? constraintDef.getMinElements().toString() : EMPTY_STRING));
            sb.append(listItem(numOfTab + 1, "max elements", constraintDef.getMaxElements() != null ? constraintDef.getMaxElements().toString() : EMPTY_STRING));
        }
        return sb.toString();
    }

    private String identities(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<IdentitySchemaNode> identities = module.getIdentities();

        if (identities.isEmpty()) {
            return EMPTY_STRING;
        }

        sb.append(indentWithNewLine(numOfTab, "<h2>Identities</h2>"));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));

        for (final IdentitySchemaNode identity : identities) {
            sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
            sb.append(indentWithNewLine(numOfTab + 2, concat("<h3 id=\">", identity.getQName().getLocalName(), "\">", identity.getQName().getLocalName(), "</h3>")));
            sb.append(indentWithNewLine(numOfTab + 2, "<ul>"));
            sb.append(descAndRefLi(numOfTab + 2, identity));

            if (identity.getBaseIdentity() != null) {
                sb.append(listItem(numOfTab + 2, "base", identity.getQName().getLocalName()));
            }
            sb.append(indentWithNewLine(numOfTab + 2, "</ul>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
        }
        sb.append(indentWithNewLine(numOfTab, "</ul>"));

        return sb.toString();
    }

    private String typeDefinitions(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<TypeDefinition<?>> typedefs = module.getTypeDefinitions();

        if (typedefs.isEmpty()) {
            return EMPTY_STRING;
        }
        sb.append(indentWithNewLine(numOfTab, "<h2>Type Definitions</h2>"));
        sb.append(indentWithNewLine(numOfTab, "<ul>"));

        for (final TypeDefinition<?> typedef : typedefs) {
            sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
            sb.append(indentWithNewLine(
                    numOfTab + 2,
                    concat("<h3 id=\">", typedef.getQName().getLocalName(), "\">", typedef.getQName().getLocalName(),
                            "</h3>")));
            sb.append(indentWithNewLine(numOfTab + 2, "<ul>"));
            sb.append(descAndRefLi(numOfTab + 2, typedef));
            sb.append(restrictions(numOfTab + 2, typedef));
            sb.append(indentWithNewLine(numOfTab + 2, "</ul>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
        }

        sb.append(indentWithNewLine(numOfTab, "</ul>"));

        return sb.toString();
    }

    private String descAndRefLi(final int numOfTab, final SchemaNode schemaNode) {
        final StringBuilder sb = new StringBuilder();

        sb.append(listItem(numOfTab, "Description", schemaNode.getDescription()));
        sb.append(listItem(numOfTab, "Reference", schemaNode.getReference()));

        return sb.toString();
    }

    private String listItem(final int numOfTab, final String itemName, final String itemDescription) {
        final StringBuilder sb = new StringBuilder();

        if (itemDescription != null && !itemDescription.isEmpty()) {
            sb.append(indentWithNewLine(numOfTab, "<li>"));
            sb.append(indentWithNewLine(numOfTab + 1, concat(itemName, ": ", itemDescription)));
            sb.append(indentWithNewLine(numOfTab, "</li>"));
        }
        return sb.toString();
    }

    private String listItem(final int numOfTab, final String itemDescription) {
        final StringBuilder sb = new StringBuilder();

        if (itemDescription != null && !itemDescription.isEmpty()) {
            sb.append(indentWithNewLine(numOfTab, "<li>"));
            sb.append(indentWithNewLine(numOfTab + 1, itemDescription));
            sb.append(indentWithNewLine(numOfTab, "</li>"));
        }
        return sb.toString();
    }

    private String restrictions(final int numOfTab, final TypeDefinition<?> type) {
        final StringBuilder sb = new StringBuilder();

        sb.append(toBaseStmt(numOfTab, type));
        sb.append(toLength(numOfTab, type));
        sb.append(toRange(numOfTab, type));

        return sb.toString();
    }

    private String toBaseStmt(final int numOfTab, final TypeDefinition<?> baseType) {
        final StringBuilder sb = new StringBuilder();

        if (baseType != null) {
            sb.append(listItem(numOfTab, "Base type",
                    typeAnchorLink(baseType.getPath(), baseType.getQName().getLocalName())));
        }
        return sb.toString();
    }

    private String toLength(final int numOfTab, final TypeDefinition<?> baseType) {
        return EMPTY_STRING;
    }

    private String toLength(final int numOfTab, final BinaryTypeDefinition type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toLengthStmt(numOfTab, type.getLengthConstraints()));
        return sb.toString();
    }

    private String toLength(final int numOfTab, final StringTypeDefinition type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toLengthStmt(numOfTab, type.getLengthConstraints()));
        return sb.toString();
    }

    private String toLength(final int numOfTab, final ExtendedType type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toLengthStmt(numOfTab, type.getLengthConstraints()));
        return sb.toString();
    }

    private String toRange(final int numOfTab, final TypeDefinition<?> baseType) {
        return EMPTY_STRING;
    }

    private String toRange(final int numOfTab, final DecimalTypeDefinition type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toRangeStmt(numOfTab, type.getRangeConstraints()));
        return sb.toString();
    }

    private String toRange(final int numOfTab, final IntegerTypeDefinition type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toRangeStmt(numOfTab, type.getRangeConstraints()));
        return sb.toString();
    }

    private String toRange(final int numOfTab, final UnsignedIntegerTypeDefinition type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toRangeStmt(numOfTab, type.getRangeConstraints()));
        return sb.toString();
    }

    private String toRange(final int numOfTab, final ExtendedType type) {
        final StringBuilder sb = new StringBuilder();
        sb.append(toRangeStmt(numOfTab, type.getRangeConstraints()));
        return sb.toString();
    }

    private String toLengthStmt(final int numOfTab, final Collection<LengthConstraint> lengthConstraints) {
        final StringBuilder sb = new StringBuilder();

        if (lengthConstraints != null && !lengthConstraints.isEmpty()) {
            sb.append(listItem(numOfTab, "Length restrictions:"));
            sb.append(indentWithNewLine(numOfTab, "<ul>"));

            for (final LengthConstraint length : lengthConstraints) {
                sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
                if (length.getMin() == length.getMax()) {
                    sb.append(indentWithNewLine(numOfTab + 2, length.getMin().toString()));
                } else {
                    sb.append(indentWithNewLine(numOfTab + 2,
                            concat("&lt;", length.getMin().toString(), ", ", length.getMax().toString(), "&gt;")));
                }
                sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
            }
            sb.append(indentWithNewLine(numOfTab, "</ul>"));
        }

        return sb.toString();
    }

    private String toRangeStmt(final int numOfTab, final Collection<RangeConstraint> rangeConstraints) {
        final StringBuilder sb = new StringBuilder();

        if (rangeConstraints != null && !rangeConstraints.isEmpty()) {
            sb.append(listItem(numOfTab, "Length restrictions:"));
            sb.append(indentWithNewLine(numOfTab, "<ul>"));

            for (final RangeConstraint range : rangeConstraints) {
                sb.append(indentWithNewLine(numOfTab + 1, "<li>"));
                if (range.getMin() == range.getMax()) {
                    sb.append(indentWithNewLine(numOfTab + 2, range.getMin().toString()));
                } else {
                    sb.append(indentWithNewLine(numOfTab + 2,
                            concat("&lt;", range.getMin().toString(), ", ", range.getMax().toString(), "&gt;")));
                }
                sb.append(indentWithNewLine(numOfTab + 1, "</li>"));
            }
            sb.append(indentWithNewLine(numOfTab, "</ul>"));
        }

        return sb.toString();
    }

    private String typeAnchorLink(final SchemaPath schemaPath, final String localName) {
        final StringBuilder sb = new StringBuilder();

        if (schemaPath != null) {
            final QName lastPathElement = Iterables.getLast(schemaPath.getPathFromRoot());
            final URI namespace = lastPathElement.getNamespace();

            if (namespace == this.currentModule.getNamespace()) {
                return concat("<a href=\"#", schemaPathToId(schemaPath), "\">", localName, "</a>");
            } else {
                return concat("(", namespace.toString(), ")", localName);
            }
        }
        return sb.toString();
    }

    private String featuresSummary(int numOfTab, Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<FeatureDefinition> features = module.getFeatures();

        if (features.isEmpty()) {
            return EMPTY_STRING;
        } else {
            sb.append(indentWithNewLine(numOfTab, "<h3>Features Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Name</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

            for (final FeatureDefinition feature : features) {
                sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 2,
                        anchorLink(feature.getQName().getLocalName(), strong(feature.getQName().getLocalName()))));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1, feature.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
            }
        }
        sb.append(indentWithNewLine(numOfTab, "</table>"));

        return sb.toString();
    }

    private Object extensionsSummary(int numOfTab, Module module) {
        final StringBuilder sb = new StringBuilder();
        final List<ExtensionDefinition> extensions = module.getExtensionSchemaNodes();

        if (extensions.isEmpty()) {
            return EMPTY_STRING;
        } else {
            sb.append(indentWithNewLine(numOfTab, "<h3>Extensions Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Name</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

            for (final ExtensionDefinition extension : extensions) {
                sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 2,
                        anchorLink(extension.getQName().getLocalName(), strong(extension.getQName().getLocalName()))));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1, extension.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
            }
        }
        sb.append(indentWithNewLine(numOfTab, "</table>"));

        return sb.toString();
    }

    private String rpcsSummary(int numOfTab, Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<RpcDefinition> rpcs = module.getRpcs();

        if (rpcs.isEmpty()) {
            return EMPTY_STRING;
        } else {
            sb.append(indentWithNewLine(numOfTab, "<h3>RPCs Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Name</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

            for (final RpcDefinition rpc : rpcs) {
                sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 2,
                        anchorLink(rpc.getQName().getLocalName(), strong(rpc.getQName().getLocalName()))));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1, rpc.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
            }
        }
        sb.append(indentWithNewLine(numOfTab, "</table>"));

        return sb.toString();
    }

    private String notificationsSummary(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<NotificationDefinition> notifications = module.getNotifications();

        if (notifications.isEmpty()) {
            return EMPTY_STRING;
        } else {
            sb.append(indentWithNewLine(numOfTab, "<h3>Notification Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Name</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

            for (final NotificationDefinition notification : notifications) {
                sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(
                        numOfTab + 2,
                        anchorLink(schemaPathToId(notification.getPath()), strong(notification.getQName()
                                .getLocalName()))));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1, notification.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
            }
        }
        sb.append(indentWithNewLine(numOfTab, "</table>"));

        return sb.toString();
    }

    private final String schemaPathToId(final SchemaPath schemaPath) {
        final StringBuilder sb = new StringBuilder();

        if (path != null) {
            for (final QName pathSegment : schemaPath.getPathFromRoot()) {
                sb.append(concat(pathSegment.getLocalName(), "/"));
            }
            return sb.toString();
        } else {
            return EMPTY_STRING;
        }
    }

    private Object objectsSummary(int numOfTab, Module module) {
        final StringBuilder sb = new StringBuilder();
        final Collection<DataSchemaNode> childNodes = module.getChildNodes();

        if (childNodes.isEmpty()) {
            return EMPTY_STRING;
        } else {
            sb.append(indentWithNewLine(numOfTab, "<h3>Child Nodes Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Name</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

            for (DataSchemaNode childNode : childNodes) {
                sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 2,
                        anchorLink(childNode.getQName().getLocalName(), strong(childNode.getQName().getLocalName()))));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1, childNode.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
            }
        }
        sb.append(indentWithNewLine(numOfTab, "</table>"));

        return sb.toString();
    }

    private Object augmentationsSummary(final int numOfTab, final Module module, final SchemaContext context) {
        final StringBuilder sb = new StringBuilder();
        final Set<AugmentationSchema> augmentations = module.getAugmentations();

        if (augmentations.isEmpty()) {
            return EMPTY_STRING;
        } else {
            sb.append(indentWithNewLine(numOfTab, "<h3>Augmentations Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Target</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

            for (AugmentationSchema augment : augmentations) {
                sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 1, "<td>"));
                sb.append(indentWithNewLine(
                        numOfTab + 1,
                        anchorLink(schemaPathToString(module, augment.getTargetPath(), context, augment),
                                strong(schemaPathToString(module, augment.getTargetPath(), context, augment)))));
                sb.append(indentWithNewLine(numOfTab + 1, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1, augment.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 1, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
            }
            sb.append(indentWithNewLine(numOfTab, "</table>"));
        }
        return sb.toString();
    }

    private String schemaPathToString(final Module module, final SchemaPath schemaPath, final SchemaContext ctx,
            final DataNodeContainer dataNode) {
        final Iterable<QName> path = schemaPath.getPathFromRoot();
        final StringBuilder pathString = new StringBuilder();

        if (schemaPath.isAbsolute()) {
            pathString.append('/');
        }

        final QName moduleQName = path.iterator().next();
        Object parent = ctx.findModuleByNamespaceAndRevision(moduleQName.getNamespace(), moduleQName.getRevision());

        for (final QName name : path) {
            if (parent instanceof DataNodeContainer) {
                SchemaNode node = ((DataNodeContainer) parent).getDataChildByName(name);
                if (node == null && (parent instanceof Module)) {
                    for (final NotificationDefinition notification : ((Module) parent).getNotifications()) {
                        if (notification.getQName().equals(name)) {
                            node = notification;
                        }
                    }
                }
                if (node == null && (parent instanceof Module)) {
                    for (final RpcDefinition rpc : ((Module) parent).getRpcs()) {
                        if (rpc.getQName().equals(name)) {
                            node = rpc;
                        }
                    }
                }

                final Module pathElementModule = ctx.findModuleByNamespaceAndRevision(name.getNamespace(),
                        name.getRevision());
                final String moduleName = pathElementModule.getName();
                pathString.append(moduleName);
                pathString.append(":");
                pathString.append(name.getLocalName());
                pathString.append("/");

                if (node instanceof ChoiceNode && dataNode != null) {
                    final ChoiceCaseNode caseNode = getFirstChoiceCase(dataNode.getChildNodes());
                    if (caseNode != null) {
                        pathString.append("(case)");
                        pathString.append(caseNode.getQName().getLocalName());
                    }
                }
                parent = node;
            }
        }
        return pathString.toString();
    }

    private ChoiceCaseNode getFirstChoiceCase(final Iterable<DataSchemaNode> nodes) {
        ChoiceCaseNode findedNode = null;

        for (final DataSchemaNode node : nodes) {
            if (node instanceof ChoiceCaseNode) {
                findedNode = (ChoiceCaseNode) node;
            }
        }
        return findedNode;
    }

    private Object groupingsSummary(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<GroupingDefinition> groupings = module.getGroupings();

        if (groupings.isEmpty()) {
            return EMPTY_STRING;
        } else {
            sb.append(indentWithNewLine(numOfTab, "<h3>Groupings Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Name</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

            for (GroupingDefinition grouping : groupings) {
                sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 1, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1,
                        anchorLink(grouping.getQName().getLocalName(), strong(grouping.getQName().getLocalName()))));
                sb.append(indentWithNewLine(numOfTab + 1, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 1, grouping.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 1, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
            }
        }
        return sb.toString();
    }

    private Object identitiesSummary(int numOfTab, Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<IdentitySchemaNode> identities = module.getIdentities();

        if (identities.isEmpty()) {
            return sb.toString();
        } else {

        }

        sb.append(indentWithNewLine(numOfTab, "<h3>Identities Summary</h3>"));
        sb.append(indentWithNewLine(numOfTab, "<table>"));
        sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
        sb.append(indentWithNewLine(numOfTab + 2, "<th>Name</th>"));
        sb.append(indentWithNewLine(numOfTab + 2, "<th>Description</th>"));
        sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

        for (final IdentitySchemaNode identity : identities) {
            sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
            sb.append(indentWithNewLine(numOfTab + 2,
                    anchorLink(identity.getQName().getLocalName(), strong(identity.getQName().getLocalName()))));
            sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<td>"));
            sb.append(indentWithNewLine(numOfTab, identity.getDescription()));
            sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
            sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
        }
        sb.append(indentWithNewLine(numOfTab, "</table>"));

        return sb.toString();
    }

    private String typeDefinitionsSummary(int numOfTab, Module module) {
        final StringBuilder sb = new StringBuilder();
        final Set<TypeDefinition<?>> typedefs = module.getTypeDefinitions();

        if (!typedefs.isEmpty()) {
            sb.append(indentWithNewLine(numOfTab, "<div>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<h3>Type Definitions Summary</h3>"));
            sb.append(indentWithNewLine(numOfTab + 1, "<table>"));
            sb.append(indentWithNewLine(numOfTab + 2, "<tr>"));
            sb.append(indentWithNewLine(numOfTab + 3, "<th>Name</th>"));
            sb.append(indentWithNewLine(numOfTab + 3, "<th>Description</th>"));
            sb.append(indentWithNewLine(numOfTab + 2, "</tr>"));

            for (final TypeDefinition<?> typedef : typedefs) {
                sb.append(indentWithNewLine(numOfTab + 2, "<tr>"));
                sb.append(indentWithNewLine(numOfTab + 3, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 3,
                        anchorLink(typedef.getQName().getLocalName(), strong(typedef.getQName().getLocalName()))));
                sb.append(indentWithNewLine(numOfTab + 3, "</td>"));

                sb.append(indentWithNewLine(numOfTab + 3, "<td>"));
                sb.append(indentWithNewLine(numOfTab + 3, typedef.getDescription()));
                sb.append(indentWithNewLine(numOfTab + 3, "</td>"));
                sb.append(indentWithNewLine(numOfTab + 2, "</tr>"));
            }

            sb.append(indentWithNewLine(numOfTab + 1, "<table>"));
            sb.append(indentWithNewLine(numOfTab, "</div>"));
        }

        return sb.toString();
    }

    private String anchorLink(final String anchor, final String text) {
        return concat("<a href=\"#", anchor, "\">", text, "</a>");
    }

    private String strong(final String text) {
        return concat("<strong>", text, "</strong>");
    }

    private String header(final int numOfTab, final int level, final YangInstanceIdentifier name) {
        final StringBuilder sb = new StringBuilder();

        sb.append("<h");
        sb.append(level);
        sb.append(" id=\"");

        for (final PathArgument pathArg : name.getPathArguments()) {
            sb.append(concat(pathArg.getNodeType().getLocalName(), "/"));
        }
        sb.append("\">");

        for (final PathArgument pathArg : name.getPathArguments()) {
            sb.append(concat(pathArg.getNodeType().getLocalName(), "/"));
        }

        sb.append(concat("</h", String.valueOf(level), ">"));

        return indentWithNewLine(numOfTab, sb.toString());
    }

    private String header(final int numOfTab, final Module module) {
        final StringBuilder sb = new StringBuilder();

        sb.append(indentWithNewLine(numOfTab, concat("<h1>", module.getName(), "</h1>")));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfTab, "<h2>Base Information</h2>"));
        sb.append(indentWithNewLine(numOfTab, "<table>"));
        sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
        sb.append(indentWithNewLine(numOfTab + 2, "<td>strong(\"prefix\")</td>"));
        sb.append(indentWithNewLine(numOfTab + 2, concat("<td>", module.getPrefix(), "</td>")));
        sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

        sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
        sb.append(indentWithNewLine(numOfTab + 2, "<td>strong(\"namespace\")</td>"));
        sb.append(indentWithNewLine(numOfTab + 2, concat("<td>", module.getPrefix(), "</td>")));
        sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

        sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
        sb.append(indentWithNewLine(numOfTab + 2, "<td>strong(\"revision\")</td>"));
        sb.append(indentWithNewLine(numOfTab + 2, concat("<td>", module.getPrefix(), "</td>")));
        sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

        sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
        sb.append(indentWithNewLine(numOfTab + 2, "<td>strong(\"description\")</td>"));
        sb.append(indentWithNewLine(numOfTab + 2, concat("<td>", module.getPrefix(), "</td>")));
        sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

        sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
        sb.append(indentWithNewLine(numOfTab + 2, "<td>strong(\"yang-version\")</td>"));
        sb.append(indentWithNewLine(numOfTab + 2, concat("<td>", module.getPrefix(), "</td>")));
        sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));

        sb.append(indentWithNewLine(numOfTab + 1, "<tr>"));
        for (final ModuleImport moduleImport : module.getImports()) {
            sb.append(NEW_LINE);
            sb.append(indentWithNewLine(numOfTab + 2, "<td>strong(\"imports\")</td><td>"));
            sb.append(NEW_LINE);
            sb.append(indentWithNewLine(3, concat(moduleImport.getPrefix(), ":", moduleImport.getModuleName())));
            if (moduleImport.getRevision() != null) {
                sb.append(concat(" ", REVISION_FORMAT.format(moduleImport.getRevision())));
            }
            sb.append(indentWithNewLine(numOfTab + 2, "</td>"));
            sb.append(NEW_LINE);
        }

        sb.append(indentWithNewLine(numOfTab + 1, "</tr>"));
        sb.append(NEW_LINE);
        sb.append(indentWithNewLine(numOfTab, "</table>"));

        return sb.toString();
    }
}
