/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * yang-data-util
 * org.opendaylight.yangtools.yang.data.util
 * Utility class for transforming xml format notification to Notification Object.
 * @author <a href="mailto:geng.xingyuan@zte.com.cn">Geng Xingyuan</a>
 */
public final class NotificationAsContainer implements ContainerSchemaNode {

    private final Map<QName, DataSchemaNode> mapNodes;
    private final NotificationDefinition notification;

    @Beta
    public NotificationAsContainer(final NotificationDefinition notification) {
        this.notification = Preconditions.checkNotNull(notification);
        mapNodes = Maps.uniqueIndex(notification.getChildNodes(), DataSchemaNode::getQName);
    }

    @Override
    public String getDescription() {
        return notification.getDescription();
    }

    @Override
    public String getReference() {
        return notification.getReference();
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return notification.getTypeDefinitions();
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return notification.getGroupings();
    }

    @Override
    public Status getStatus() {
        return notification.getStatus();
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return notification.getChildNodes();
    }

    @Override
    public DataSchemaNode getDataChildByName(final QName qname) {
        return mapNodes.get(qname);
    }

    @Override
    public Set<UsesNode> getUses() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isPresenceContainer() {
        return false;
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return notification.getAvailableAugmentations();
    }

    @Override
    public boolean isAugmenting() {
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        //FIXME: reference to https://bugs.opendaylight.org/show_bug.cgi?id=6897
        return false;
    }

    @Override
    public boolean isConfiguration() {
        return false;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        // FIXME: use EmptyConstraintDefinition.OPTIONAL,but EmptyConstraintDefinition is not public
        return null;
    }

    @Override
    public QName getQName() {
        return notification.getQName();
    }

    @Override
    public SchemaPath getPath() {
        return notification.getPath();
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return ImmutableList.of();
    }
}
