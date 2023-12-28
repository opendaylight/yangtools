/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractUnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

abstract sealed class ImmutableUnkeyedListNode extends AbstractUnkeyedListNode {
    static final class Empty extends ImmutableUnkeyedListNode {
        Empty(final NodeIdentifier name) {
            super(name);
        }

        @Override
        public ImmutableList<UnkeyedListEntryNode> value() {
            return ImmutableList.of();
        }

        @Override
        public UnkeyedListEntryNode childAt(final int position) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        protected int valueHashCode() {
            return 1;
        }

        @Override
        protected boolean valueEquals(final UnkeyedListNode other) {
            return other.isEmpty();
        }
    }

    private static final class Regular extends ImmutableUnkeyedListNode {
        private final @NonNull ImmutableList<UnkeyedListEntryNode> children;

        Regular(final NodeIdentifier name, final ImmutableList<UnkeyedListEntryNode> children) {
            super(name);
            this.children = requireNonNull(children);
        }

        @Override
        public UnkeyedListEntryNode childAt(final int position) {
            return children.get(position);
        }

        @Override
        public int size() {
            return children.size();
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected ImmutableList<UnkeyedListEntryNode> value() {
            return children;
        }

        @Override
        protected boolean valueEquals(final UnkeyedListNode other) {
            final Collection<UnkeyedListEntryNode> otherChildren;
            if (other instanceof Regular immutableOther) {
                otherChildren = immutableOther.children;
            } else {
                otherChildren = other.body();
            }
            return otherChildren instanceof List ? children.equals(otherChildren)
                : Iterables.elementsEqual(children, otherChildren);
        }
    }

    private final @NonNull NodeIdentifier name;

    private ImmutableUnkeyedListNode(final NodeIdentifier name) {
        this.name = requireNonNull(name);
    }

    static final @NonNull ImmutableUnkeyedListNode of(final NodeIdentifier name,
            final List<UnkeyedListEntryNode> value) {
        return value.isEmpty() ? new Empty(name) : new Regular(name, ImmutableList.copyOf(value));
    }

    @Override
    public final NodeIdentifier name() {
        return name;
    }

    @Override
    protected final ImmutableList<UnkeyedListEntryNode> wrappedValue() {
        return value();
    }

    @Override
    protected abstract ImmutableList<UnkeyedListEntryNode> value();
}