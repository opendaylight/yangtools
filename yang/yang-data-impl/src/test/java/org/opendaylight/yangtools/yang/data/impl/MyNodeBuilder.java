/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import groovy.util.BuilderSupport;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michal.rehak
 */
@Deprecated
public class MyNodeBuilder extends BuilderSupport {

    private static final Logger LOG = LoggerFactory.getLogger(MyNodeBuilder.class);

    private URI qnNamespace;
    private final Date qnRevision;

    private CompositeNode rootNode;

    /**
     * @param baseQName
     */
    private MyNodeBuilder(final QName baseQName) {
        qnNamespace = baseQName.getNamespace();
        qnRevision = baseQName.getRevision();
    }

    /**
     * @return initialized singleton instance
     */
    public static MyNodeBuilder newInstance() {
        QName qName = null;
        try {
            qName = QName.create(
                    new URI("urn:opendaylight:controller:network"),
                    new Date(42), "node");
        } catch (URISyntaxException e) {
            LOG.error(e.getMessage(), e);
        }
        return new MyNodeBuilder(qName);
    }

    @Override
    protected void setParent(final Object parent, final Object child) {
        // do nothing
        if (child instanceof AbstractNodeTO<?>) {
            ((AbstractNodeTO<?>) child).setParent((CompositeNode) parent);
        } else {
            LOG.error("PARENTING FAILED: "+parent + " -> " + child);
        }
    }

    @Override
    protected Object createNode(final Object name) {
        MutableCompositeNode newNode = NodeFactory.createMutableCompositeNode(
                createQName(name), getCurrentNode(), null, null, null);
        NodeUtils.fixParentRelation(newNode);
        return newNode;
    }

    @Override
    protected Object createNode(final Object name, @SuppressWarnings("rawtypes") final Map attributes) {
        ModifyAction modifyAction = processAttributes(attributes);
        MutableCompositeNode newNode = NodeFactory.createMutableCompositeNode(
                createQName(name), getCurrentNode(), null, modifyAction, null);
        NodeUtils.fixParentRelation(newNode);
        return newNode;
    }


    @Override
    protected Object createNode(final Object name, @SuppressWarnings("rawtypes") final Map attributes, final Object value) {
        ModifyAction modifyAction = processAttributes(attributes);
        SimpleNode<Object> newNode = NodeFactory.createImmutableSimpleNode(
                createQName(name), (CompositeNode) getCurrent(), value, modifyAction);
        NodeUtils.fixParentRelation(newNode);
        return newNode;
    }

    /**
     * @param attributes
     * @return
     */
    private ModifyAction processAttributes(@SuppressWarnings("rawtypes") final Map attributes) {
        LOG.debug("attributes:" + attributes);
        ModifyAction modAction = null;

        @SuppressWarnings("unchecked")
        Map<String, String> attributesSane = attributes;
        for (Entry<String, String> attr : attributesSane.entrySet()) {
            switch (attr.getKey()) {
            case "xmlns":
                try {
                    qnNamespace = new URI(attr.getValue());
                } catch (URISyntaxException e) {
                    LOG.error(e.getMessage(), e);
                }
                break;
            case "modifyAction":
                modAction = ModifyAction.valueOf(attr.getValue());
                break;

            default:
                throw new IllegalArgumentException("Attribute not supported: "+attr.getKey());
            }
        }
        return modAction;
    }

    @Override
    protected Object createNode(final Object name, final Object value) {
        SimpleNode<Object> newNode = NodeFactory.createImmutableSimpleNode(createQName(name), (CompositeNode) getCurrent(), value);
        NodeUtils.fixParentRelation(newNode);
        return newNode;
    }

    private QName createQName(final Object localName) {
        LOG.debug("qname for: "+localName);
        return QName.create(qnNamespace, qnRevision, (String) localName);
    }

    protected CompositeNode getCurrentNode() {
        if (getCurrent() != null) {
            if (getCurrent() instanceof CompositeNode) {
                return (CompositeNode) getCurrent();

            } else {
                throw new IllegalAccessError("current node is not of type CompositeNode, but: "
                        +getCurrent().getClass().getSimpleName());
            }
        }

        return null;
    }

    @Override
    protected Object postNodeCompletion(final Object parent, final Object node) {
        Node<?> nodeRevisited = (Node<?>) node;
        LOG.debug("postNodeCompletion at: \n  "+ nodeRevisited+"\n  "+parent);
        if (nodeRevisited instanceof MutableCompositeNode) {
            MutableCompositeNode mutant = (MutableCompositeNode) nodeRevisited;
            if (mutant.getValue().isEmpty()) {
                LOG.error("why is it having empty value? -- " + mutant);
            }
            nodeRevisited = NodeFactory.createImmutableCompositeNode(
                    mutant.getNodeType(), mutant.getParent(), mutant.getValue(), mutant.getModificationAction());
            NodeUtils.fixChildrenRelation((CompositeNode) nodeRevisited);

            if (parent == null) {
                rootNode = (CompositeNode) nodeRevisited;
            } else {
                NodeUtils.fixParentRelation(nodeRevisited);
                nodeRevisited.getParent().getValue().remove(mutant);
            }
        }


        return nodeRevisited;
    }

    /**
     * @return tree root
     */
    public CompositeNode getRootNode() {
        return rootNode;
    }
}
