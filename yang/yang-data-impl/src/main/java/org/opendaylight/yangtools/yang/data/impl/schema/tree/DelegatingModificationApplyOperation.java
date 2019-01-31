/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

abstract class DelegatingModificationApplyOperation extends ModificationApplyOperation {
    @Override
    final ChildTrackingPolicy getChildPolicy() {
        return delegate().getChildPolicy();
    }

    @Override
    final void mergeIntoModifiedNode(final ModifiedNode node, final NormalizedNode<?, ?> value, final Version version) {
        delegate().mergeIntoModifiedNode(node, value, version);
    }

    @Override
    public final Optional<ModificationApplyOperation> getChild(final PathArgument child) {
        return delegate().getChild(child);
    }

    @Override
    final void quickVerifyStructure(final NormalizedNode<?, ?> modification) {
        delegate().quickVerifyStructure(modification);
    }

    @Override
    final void recursivelyVerifyStructure(final NormalizedNode<?, ?> value) {
        delegate().recursivelyVerifyStructure(value);
    }

    /**
     * Return the underlying delegate.
     *
     * @return Underlying delegate.
     */
    abstract ModificationApplyOperation delegate();
}
