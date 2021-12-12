/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

abstract class AbstractAsContainer extends ForwardingObject implements ContainerLike {
    @Override
    protected abstract @NonNull SchemaNode delegate();

    @Override
    public final Optional<String> getDescription() {
        return delegate().getDescription();
    }

    @Override
    public final Optional<String> getReference() {
        return delegate().getReference();
    }

    @Override
    public final Status getStatus() {
        return delegate().getStatus();
    }

    @Override
    public final QName getQName() {
        return delegate().getQName();
    }

    @Deprecated
    @Override
    public final boolean isAugmenting() {
        return false;
    }

    @Override
    @Deprecated(forRemoval = true)
    public final boolean isAddedByUses() {
        return false;
    }

    @Override
    public final Optional<Boolean> effectiveConfig() {
        return Optional.empty();
    }

    @Override
    public final Collection<? extends ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public final Collection<? extends NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }

    @Override
    public final Collection<? extends UsesNode> getUses() {
        return ImmutableSet.of();
    }

    @Override
    public final Collection<? extends @NonNull MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public final Optional<? extends QualifiedBound> getWhenCondition() {
        return Optional.empty();
    }
}
