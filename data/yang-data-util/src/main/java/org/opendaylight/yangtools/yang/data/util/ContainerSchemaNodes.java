/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Utility class for taking notification or rpc as ContainerSchemaNode.
 *
 * @author <a href="mailto:geng.xingyuan@zte.com.cn">Geng Xingyuan</a>
 * @deprecated This class is deprecated for removal. Use {@link NotificationDefinition#toContainerLike()} and
 *             {@link OperationDefinition#toContainerLike()} to can similar service. Note that the former differs from
 *             {@link #forNotification(NotificationDefinition)} in that it does not report the source notification from
 *             {@link NotificationNodeContainer#getNotifications()} -- i.e. it does not contain itself.
 */
@Deprecated(since = "11.0.0", forRemoval = true)
public final class ContainerSchemaNodes {
    private ContainerSchemaNodes() {
        // Hidden on purpose
    }

    public static @NonNull ContainerLike forNotification(final NotificationDefinition notification) {
        return new NotificationContainerSchemaNode(notification);
    }

    public static @NonNull ContainerLike forRPC(final RpcDefinition rpc) {
        return rpc.toContainerLike();
    }

    private static final class NotificationContainerSchemaNode implements ContainerLike {
        final @NonNull NotificationDefinition schemaNode;

        NotificationContainerSchemaNode(final NotificationDefinition schemaNode) {
            this.schemaNode = requireNonNull(schemaNode);
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
        public QName getQName() {
            return schemaNode.getQName();
        }

        @Override
        @Deprecated
        public boolean isAugmenting() {
            return false;
        }

        @Override
        @Deprecated(forRemoval = true)
        public boolean isAddedByUses() {
            return false;
        }

        @Override
        public Optional<Boolean> effectiveConfig() {
            return Optional.empty();
        }

        @Override
        public Collection<? extends ActionDefinition> getActions() {
            return ImmutableSet.of();
        }

        @Override
        public Collection<? extends UsesNode> getUses() {
            return ImmutableSet.of();
        }

        @Override
        public Collection<? extends @NonNull MustDefinition> getMustConstraints() {
            return ImmutableList.of();
        }

        @Override
        public Optional<? extends QualifiedBound> getWhenCondition() {
            return Optional.empty();
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
            return schemaNode.dataChildByName(name);
        }
    }
}
