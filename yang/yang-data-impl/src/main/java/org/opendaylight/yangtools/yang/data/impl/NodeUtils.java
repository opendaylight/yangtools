/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.NodeModification;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * @author michal.rehak
 *
 * @deprecated Use {@link NormalizedNodes} instead.
 */
@Deprecated
public abstract class NodeUtils {
    private static final Joiner DOT_JOINER = Joiner.on(".");
    private static final Logger LOG = LoggerFactory.getLogger(NodeUtils.class);
    private static final Function<QName, String> LOCALNAME_FUNCTION = new Function<QName, String>() {
        @Override
        public String apply(final @Nonnull QName input) {
            Preconditions.checkNotNull(input);
            return input.getLocalName();
        }
    };

    /**
     *
     */
    private static final String USER_KEY_NODE = "node";

    /**
     * @param node
     * @return node path up till root node
     */
    public static String buildPath(final Node<?> node) {
        List<String> breadCrumbs = new ArrayList<>();
        Node<?> tmpNode = node;
        while (tmpNode != null) {
            breadCrumbs.add(0, tmpNode.getNodeType().getLocalName());
            tmpNode = tmpNode.getParent();
        }

        return DOT_JOINER.join(breadCrumbs);
    }

    /**
     * @param treeRootNode
     * @return dom tree, containing same node structure, yang nodes are
     *         associated to dom nodes as user data
     */
    public static org.w3c.dom.Document buildShadowDomTree(final CompositeNode treeRootNode) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc = null;
        try {
            DocumentBuilder bob = dbf.newDocumentBuilder();
            doc = bob.newDocument();
        } catch (ParserConfigurationException e) {
            LOG.error("documentBuilder problem", e);
            return null;
        }

        final Deque<SimpleEntry<org.w3c.dom.Node, Node<?>>> jobQueue = new ArrayDeque<>();
        jobQueue.push(new SimpleEntry<org.w3c.dom.Node, Node<?>>(doc, treeRootNode));

        while (!jobQueue.isEmpty()) {
            SimpleEntry<org.w3c.dom.Node, Node<?>> job = jobQueue.pop();
            org.w3c.dom.Node jointPlace = job.getKey();
            Node<?> item = job.getValue();
            QName nodeType = item.getNodeType();
            Element itemEl = doc.createElementNS(nodeType.getNamespace().toString(), item.getNodeType().getLocalName());
            itemEl.setUserData(USER_KEY_NODE, item, null);
            if (item instanceof SimpleNode<?>) {
                Object value = ((SimpleNode<?>) item).getValue();
                if(value != null) {
                    itemEl.setTextContent(String.valueOf(value));
                }
            }
            if (item instanceof NodeModification) {
                ModifyAction modificationAction = ((NodeModification) item).getModificationAction();
                if (modificationAction != null) {
                    itemEl.setAttribute("modifyAction", modificationAction.toString());
                }
            }

            jointPlace.appendChild(itemEl);

            if (item instanceof CompositeNode) {
                for (Node<?> child : ((CompositeNode) item).getValue()) {
                    jobQueue.push(new SimpleEntry<org.w3c.dom.Node, Node<?>>(itemEl, child));
                }
            }
        }

        return doc;
    }

    /**
     * @param doc
     * @param xpathEx
     * @return user data value on found node
     * @throws XPathExpressionException
     */
    @SuppressWarnings("unchecked")
    public static <T> T findNodeByXpath(final org.w3c.dom.Document doc, final String xpathEx) throws XPathExpressionException {
        T userNode = null;
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(xpathEx);

        org.w3c.dom.Node result = (org.w3c.dom.Node) expr.evaluate(doc, XPathConstants.NODE);
        if (result != null) {
            userNode = (T) result.getUserData(USER_KEY_NODE);
        }

        return userNode;
    }

    /**
     * build NodeMap, where key = qName; value = node
     *
     * @param value
     * @return map of children, where key = qName and value is list of children
     *         groupped by qName
     */
    public static Map<QName, List<Node<?>>> buildNodeMap(final List<Node<?>> value) {
        Map<QName, List<Node<?>>> nodeMapTmp = Maps.newLinkedHashMap();
        if (value == null) {
            throw new IllegalStateException("nodeList should not be null or empty");
        }
        for (Node<?> node : value) {
            List<Node<?>> qList = nodeMapTmp.get(node.getNodeType());
            if (qList == null) {
                qList = new ArrayList<>();
                nodeMapTmp.put(node.getNodeType(), qList);
            }
            qList.add(node);
        }
        return nodeMapTmp;
    }

    /**
     * @param context
     * @return map of lists, where key = path; value = {@link DataSchemaNode}
     */
    public static Map<String, ListSchemaNode> buildMapOfListNodes(final SchemaContext context) {
        Map<String, ListSchemaNode> mapOfLists = new HashMap<>();

        final Deque<DataSchemaNode> jobQueue = new ArrayDeque<>();
        jobQueue.addAll(context.getDataDefinitions());

        while (!jobQueue.isEmpty()) {
            DataSchemaNode dataSchema = jobQueue.pop();
            if (dataSchema instanceof ListSchemaNode) {
                mapOfLists.put(schemaPathToPath(dataSchema.getPath().getPathFromRoot()), (ListSchemaNode) dataSchema);
            }

            if (dataSchema instanceof DataNodeContainer) {
                jobQueue.addAll(((DataNodeContainer) dataSchema).getChildNodes());
            }
        }

        return mapOfLists;
    }

    /**
     * @param qNamesPath
     * @return path
     */
    private static String schemaPathToPath(final Iterable<QName> qNamesPath) {
        return DOT_JOINER.join(Iterables.transform(qNamesPath, LOCALNAME_FUNCTION));
    }

    /**
     * add given node to it's parent's list of children
     *
     * @param newNode
     */
    public static void fixParentRelation(final Node<?> newNode) {
        if (newNode.getParent() != null) {
            List<Node<?>> siblings = newNode.getParent().getValue();
            if (!siblings.contains(newNode)) {
                siblings.add(newNode);
            }
        }
    }

    /**
     * crawl all children of given node and assign it as their parent
     *
     * @param parentNode
     */
    public static void fixChildrenRelation(final CompositeNode parentNode) {
        if (parentNode.getValue() != null) {
            for (Node<?> child : parentNode.getValue()) {
                if (child instanceof AbstractNodeTO<?>) {
                    ((AbstractNodeTO<?>) child).setParent(parentNode);
                }
            }
        }
    }

    /**
     * @param keys
     * @param dataMap
     * @return list of values of map, found by given keys
     */
    public static <T, K> List<K> collectMapValues(final List<T> keys, final Map<T, K> dataMap) {
        List<K> valueSubList = new ArrayList<>();
        for (T key : keys) {
            valueSubList.add(dataMap.get(key));
        }

        return valueSubList;
    }

    /**
     * @param nodes
     * @return list of children in list of appropriate type
     */
    public static List<Node<?>> buildChildrenList(final Node<?>... nodes) {
        return Lists.newArrayList(nodes);
    }

}
