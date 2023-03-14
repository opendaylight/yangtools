/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Utility class for taking notification or rpc as ContainerSchemaNode.
 *
 * @author <a href="mailto:geng.xingyuan@zte.com.cn">Geng Xingyuan</a>
 */
public final class ContainerSchemaNodes {
    private ContainerSchemaNodes() {
        // Hidden on purpose
    }

    @Beta
    public static @NonNull ContainerLike forNotification(final NotificationDefinition notification) {
        return new NotificationContainerSchemaNode(notification);
    }

    @Beta
    public static @NonNull ContainerLike forRPC(final RpcDefinition rpc) {
        return new RpcContainerSchemaNode(rpc);
    }

    private abstract static class AbstractContainerSchemaNode<T extends SchemaNode> implements ContainerLike {
        final @NonNull T schemaNode;

        AbstractContainerSchemaNode(final T schemaNode) {
            this.schemaNode = requireNonNull(schemaNode);
        }

        @Override
        public Collection<? extends UsesNode> getUses() {
            return ImmutableSet.of();
        }

        @Override
        @Deprecated
        public boolean isAugmenting() {
            return false;
        }

        @Override
        public Optional<Boolean> effectiveConfig() {
            return Optional.empty();
        }

        @Override
        public QName getQName() {
            return schemaNode.getQName();
        }

        @Override
        public Optional<String> getDescription() {
            return schemaNode.getDescription();
        }

        @Override
        public Optional<String> getReference() {
            return schemaNode.getReference();
        }

        @Override
        public Status getStatus() {
            return schemaNode.getStatus();
        }

        @Override
        public Collection<? extends @NonNull MustDefinition> getMustConstraints() {
            return ImmutableList.of();
        }

        @Override
        public Optional<? extends QualifiedBound> getWhenCondition() {
            return Optional.empty();
        }
    }

    private static final class RpcContainerSchemaNode extends AbstractContainerSchemaNode<RpcDefinition> {
        RpcContainerSchemaNode(final RpcDefinition rpcDefinition) {
            super(rpcDefinition);
        }

        @Override
        public Collection<? extends GroupingDefinition> getGroupings() {
            return schemaNode.getGroupings();
        }

        @Override
        public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
            return schemaNode.getTypeDefinitions();
        }

        @Override
        public Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
            return ImmutableSet.of();
        }

        @Override
        public Collection<? extends DataSchemaNode> getChildNodes() {
            return ImmutableList.of(schemaNode.getInput(), schemaNode.getOutput());
        }

        @Override
        public DataSchemaNode dataChildByName(final QName name) {
            // FIXME: also check namespace
            return switch (name.getLocalName()) {
                case "input" -> schemaNode.getInput();
                case "output" -> schemaNode.getOutput();
                default -> null;
            };
        }

        @Override
        @Deprecated(forRemoval = true)
        public boolean isAddedByUses() {
            return false;
        }

        @Override
        public Collection<? extends ActionDefinition> getActions() {
            return ImmutableSet.of();
        }

        @Override
        public Collection<? extends NotificationDefinition> getNotifications() {
            return ImmutableSet.of();
        }
    }

    private static final class NotificationContainerSchemaNode
            extends AbstractContainerSchemaNode<NotificationDefinition> {
        private final ImmutableMap<QName, ? extends DataSchemaNode> mapNodes;

        private NotificationContainerSchemaNode(final NotificationDefinition notification) {
            super(notification);
            mapNodes = Maps.uniqueIndex(notification.getChildNodes(), DataSchemaNode::getQName);
        }

        @Override
        public Collection<? extends NotificationDefinition> getNotifications() {
            return ImmutableSet.of(schemaNode);
        }

        @Override
        public Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
            return schemaNode.getAvailableAugmentations();
        }

        @Override
        public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
            return schemaNode.getTypeDefinitions();
        }

        @Override
        public Collection<? extends DataSchemaNode> getChildNodes() {
            return schemaNode.getChildNodes();
        }

        @Override
        public Collection<? extends GroupingDefinition> getGroupings() {
            return schemaNode.getGroupings();
        }

        @Override
        public DataSchemaNode dataChildByName(final QName name) {
            return mapNodes.get(requireNonNull(name));
        }

        @Override
        @Deprecated(forRemoval = true)
        public boolean isAddedByUses() {
            // FIXME: reference to https://jira.opendaylight.org/browse/YANGTOOLS-685
            return false;
        }

        @Override
        public Collection<? extends ActionDefinition> getActions() {
            return ImmutableSet.of();
        }
    }
}
