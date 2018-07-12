/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingLazyContainerNode;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 * A {@link ContainerNode} backed by a binding {@link DataObject}, with lazy instantiation of the ContainerNode view.
 *
 * @param <T> Binding DataObject type
 * @author Robert Varga
 */
@Beta
@ThreadSafe
public abstract class AbstractBindingLazyContainerNode<T extends DataObject, C> extends ForwardingObject
        implements BindingLazyContainerNode<T> {
    private final @NonNull NodeIdentifier identifier;
    private final @NonNull T bindingData;

    private volatile @Nullable ContainerNode delegate;
    @GuardedBy("this")
    private @Nullable C context;

    protected AbstractBindingLazyContainerNode(final @NonNull NodeIdentifier identifier, final @NonNull T bindingData,
            final C context) {
        this.identifier = requireNonNull(identifier);
        this.bindingData = requireNonNull(bindingData);
        this.context = context;
    }

    @Override
    public final @NonNull T getDataObject() {
        return bindingData;
    }

    @Override
    public final @NonNull NodeIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public final QName getNodeType() {
        return identifier.getNodeType();
    }

    @Override
    public final ContainerNode getDelegate() {
        return delegate();
    }

    @Override
    public Map<QName, String> getAttributes() {
        return delegate().getAttributes();
    }

    @Override
    public Object getAttributeValue(final QName name) {
        return delegate().getAttributeValue(name);
    }

    @Override
    public Collection<DataContainerChild<? extends PathArgument, ?>> getValue() {
        return delegate().getValue();
    }

    @Override
    public Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
        return delegate().getChild(child);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContainerNode)) {
            return false;
        }
        final ContainerNode other = (ContainerNode) obj;
        return delegate().equals(obj);
    }

    @Override
    protected final @NonNull ContainerNode delegate() {
        ContainerNode local = delegate;
        if (local == null) {
            synchronized (this) {
                local = delegate;
                if (local == null) {
                    local = delegate = requireNonNull(computeContainerNode(context));
                    context = null;
                }
            }
        }

        return local;
    }

    @GuardedBy("this")
    protected abstract @NonNull ContainerNode computeContainerNode(C context);
}
