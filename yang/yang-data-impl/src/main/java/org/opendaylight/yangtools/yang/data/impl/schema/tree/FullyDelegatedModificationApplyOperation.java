/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;

/**
 * A {@link ModificationApplyOperation} which delegates all invocations to a backing instance.
 */
abstract class FullyDelegatedModificationApplyOperation extends DelegatingModificationApplyOperation {
    @Override
    final Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> currentMeta,
            final Version version) {
        return delegate().apply(modification, currentMeta, version);
    }

    @Override
    final void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        delegate().checkApplicable(path, modification, current, version);
    }

    @Override
    final void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) {
        delegate().verifyStructure(modification, verifyChildren);
    }
}
