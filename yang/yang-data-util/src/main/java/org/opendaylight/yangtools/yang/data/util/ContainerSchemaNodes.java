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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * yang-data-util
 * org.opendaylight.yangtools.yang.data.util
 * Utility class for taking notification or rpc as ContainerSchemaNode.
 * @author <a href="mailto:geng.xingyuan@zte.com.cn">Geng Xingyuan</a>
 */
public final class ContainerSchemaNodes {

    private ContainerSchemaNodes() {
    }

    @Beta
    public static ContainerSchemaNode forNotification(final NotificationDefinition notification) {
        return new NotificationContainerSchemaNode(notification);
    }

    @Beta
    public static ContainerSchemaNode forRPC(final RpcDefinition rpc) {
        return new RpcContainerSchemaNode(rpc);
    }

    private abstract static class AbstractContainerSchemaNode implements ContainerSchemaNode {

        private final SchemaNode schemaNode;

        private AbstractContainerSchemaNode(final SchemaNode schemaNode) {
            this.schemaNode = schemaNode;
        }

        @Override
        public boolean isPresenceContainer() {
            return false;
        }

        @Override
        public Set<UsesNode> getUses() {
            return ImmutableSet.of();
        }

        @Override
        public boolean isAugmenting() {
            return false;
        }

        @Override
        public boolean isConfiguration() {
            return false;
        }

        @Nonnull
        @Override
        public QName getQName() {
            return schemaNode.getQName();
        }

        @Nonnull
        @Override
        public SchemaPath getPath() {
            return schemaNode.getPath();
        }

        @Override
        public Optional<String> getDescription() {
            return schemaNode.getDescription();
        }

        @Override
        public Optional<String> getReference() {
            return schemaNode.getReference();
        }

        @Nonnull
        @Override
        public Status getStatus() {
            return schemaNode.getStatus();
        }

        @Nonnull
        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return ImmutableList.of();
        }

        @Override
        public Collection<MustDefinition> getMustConstraints() {
            return ImmutableList.of();
        }

        @Override
        public Optional<RevisionAwareXPath> getWhenCondition() {
            return Optional.empty();
        }
    }

    private static final class RpcContainerSchemaNode extends AbstractContainerSchemaNode {

        private final RpcDefinition rpcDefinition;

        private RpcContainerSchemaNode(final RpcDefinition rpcDefinition) {
            super(rpcDefinition);
            this.rpcDefinition = rpcDefinition;
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return rpcDefinition.getGroupings();
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return rpcDefinition.getTypeDefinitions();
        }

        @Override
        public Set<AugmentationSchemaNode> getAvailableAugmentations() {
            return ImmutableSet.of();
        }

        @Override
        public Collection<DataSchemaNode> getChildNodes() {
            final ContainerSchemaNode input = rpcDefinition.getInput();
            final ContainerSchemaNode output = rpcDefinition.getOutput();
            if (input == null && output == null) {
                return ImmutableList.of();
            } else if (input != null && output != null) {
                return ImmutableList.of(input,output);
            } else if (input != null) {
                return ImmutableList.of(input);
            } else {
                return ImmutableList.of(output);
            }
        }

        @Override
        public Optional<DataSchemaNode> findDataChildByName(final QName name) {
            switch (name.getLocalName()) {
                case "input":
                    return Optional.of(rpcDefinition.getInput());
                case "output":
                    return Optional.of(rpcDefinition.getOutput());
                default:
                    return Optional.empty();
            }
        }

        @Deprecated
        @Override
        public boolean isAddedByUses() {
            return false;
        }

        @Override
        public Set<ActionDefinition> getActions() {
            return ImmutableSet.of();
        }

        @Override
        public Set<NotificationDefinition> getNotifications() {
            return ImmutableSet.of();
        }
    }

    private static final class NotificationContainerSchemaNode extends AbstractContainerSchemaNode {

        private final NotificationDefinition notification;
        private final ImmutableMap<QName, DataSchemaNode> mapNodes;

        private NotificationContainerSchemaNode(final NotificationDefinition notification) {
            super(notification);
            this.notification = notification;
            mapNodes = Maps.uniqueIndex(notification.getChildNodes(), DataSchemaNode::getQName);
        }

        @Override
        public Set<NotificationDefinition> getNotifications() {
            return ImmutableSet.of(notification);
        }

        @Override
        public Set<AugmentationSchemaNode> getAvailableAugmentations() {
            return notification.getAvailableAugmentations();
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return notification.getTypeDefinitions();
        }

        @Override
        public Collection<DataSchemaNode> getChildNodes() {
            return notification.getChildNodes();
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return notification.getGroupings();
        }

        @Override
        public Optional<DataSchemaNode> findDataChildByName(final QName name) {
            return Optional.ofNullable(mapNodes.get(requireNonNull(name)));
        }

        @Override
        @Deprecated
        public boolean isAddedByUses() {
            //FIXME: reference to https://bugs.opendaylight.org/show_bug.cgi?id=6897
            return false;
        }

        @Override
        public Set<ActionDefinition> getActions() {
            return ImmutableSet.of();
        }
    }
}
