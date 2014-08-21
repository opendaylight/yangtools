/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.yang.data.codec.gson.helpers;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class SchemaContextUtils {
    private SchemaContext schemaContext;
    private static final SchemaContextUtils INSTANCE = new SchemaContextUtils();


    private SchemaContextUtils() {
    }

    public static SchemaContextUtils getInstance() {
        return INSTANCE;
    }

    public void setSchemaContext(SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }


    public URI findNamespaceByModuleName(final String moduleName) {
        final Module module = this.findModuleByName(moduleName);
        return module == null ? null : module.getNamespace();
    }


    public Module findModuleByName(final String moduleName) {
        checkPreconditions();
        Preconditions.checkArgument(moduleName != null && !moduleName.isEmpty());
        return schemaContext.findModuleByName(moduleName, null);
    }

    public Module findModuleByNamespace(final URI namespace) {
        this.checkPreconditions();
        Preconditions.checkArgument(namespace != null);
        return schemaContext.findModuleByNamespaceAndRevision(namespace, null);
    }

    private void checkPreconditions() {
        if (schemaContext == null) {
            throw new IllegalStateException("Schema context isn't set.");
        }
    }

    public DataSchemaNode findInstanceDataChildByNameAndNamespace(final DataNodeContainer container, final String name,
            final URI namespace) {
        Preconditions.<URI> checkNotNull(namespace);

        final List<DataSchemaNode> potentialSchemaNodes = findInstanceDataChildrenByName(container, name);

        Predicate<DataSchemaNode> filter = new Predicate<DataSchemaNode>() {
            @Override
            public boolean apply(final DataSchemaNode node) {
                return Objects.equal(node.getQName().getNamespace(), namespace);
            }
        };

        Iterable<DataSchemaNode> result = Iterables.filter(potentialSchemaNodes, filter);
        return Iterables.getFirst(result, null);
    }

    public List<DataSchemaNode> findInstanceDataChildrenByName(final DataNodeContainer container, final String name) {
        Preconditions.<DataNodeContainer> checkNotNull(container);
        Preconditions.<String> checkNotNull(name);

        List<DataSchemaNode> instantiatedDataNodeContainers = new ArrayList<DataSchemaNode>();
        collectInstanceDataNodeContainers(instantiatedDataNodeContainers, container, name);
        return instantiatedDataNodeContainers;
    }

    private void collectInstanceDataNodeContainers(final List<DataSchemaNode> potentialSchemaNodes,
            final DataNodeContainer container, final String name) {

        Predicate<DataSchemaNode> filter = new Predicate<DataSchemaNode>() {
            @Override
            public boolean apply(final DataSchemaNode node) {
                return Objects.equal(node.getQName().getLocalName(), name);
            }
        };

        Iterable<DataSchemaNode> nodes = Iterables.filter(container.getChildNodes(), filter);

        // Can't combine this loop with the filter above because the filter is
        // lazily-applied by Iterables.filter.
        for (final DataSchemaNode potentialNode : nodes) {
            if (isInstantiatedDataSchema(potentialNode)) {
                potentialSchemaNodes.add(potentialNode);
            }
        }

        Iterable<ChoiceNode> choiceNodes = Iterables.filter(container.getChildNodes(), ChoiceNode.class);
        Iterable<Set<ChoiceCaseNode>> map = Iterables.transform(choiceNodes, CHOICE_FUNCTION);

        final Iterable<ChoiceCaseNode> allCases = Iterables.<ChoiceCaseNode> concat(map);
        for (final ChoiceCaseNode caze : allCases) {
            collectInstanceDataNodeContainers(potentialSchemaNodes, caze, name);
        }
    }

    public boolean isInstantiatedDataSchema(final DataSchemaNode node) {
        return node instanceof LeafSchemaNode || node instanceof LeafListSchemaNode
                || node instanceof ContainerSchemaNode || node instanceof ListSchemaNode
                || node instanceof AnyXmlSchemaNode;
    }

    private final Function<ChoiceNode, Set<ChoiceCaseNode>> CHOICE_FUNCTION = new Function<ChoiceNode, Set<ChoiceCaseNode>>() {
        @Override
        public Set<ChoiceCaseNode> apply(final ChoiceNode node) {
            return node.getCases();
        }
    };

}
