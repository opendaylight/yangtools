/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Base class supporting {@link OperationDefinition#toContainerLike()} and
 * {@link NotificationDefinition#toContainerLike()}. This class is exposed only for the unlikely need users of those
 * methods need to do tricks when encountering those containers.
 */
@Beta
public abstract class ContainerLikeCompat extends ForwardingObject implements ContainerLike {
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
    public final Set<ActionDefinition> getActions() {
        return Set.of();
    }

    @Override
    public final Set<NotificationDefinition> getNotifications() {
        return Set.of();
    }

    @Override
    public final Set<UsesNode> getUses() {
        return Set.of();
    }

    @Override
    public final Set<@NonNull MustDefinition> getMustConstraints() {
        return Set.of();
    }

    @Override
    public final Optional<? extends QualifiedBound> getWhenCondition() {
        return Optional.empty();
    }
}
