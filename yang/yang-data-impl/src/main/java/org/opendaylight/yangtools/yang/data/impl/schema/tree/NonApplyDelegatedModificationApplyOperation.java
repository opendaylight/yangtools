/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A {@link ModificationApplyOperation} which delegates all invocations except checkApplicable()/apply() to a delegate
 * instance.
 */
abstract class NonApplyDelegatedModificationApplyOperation extends DelegatingModificationApplyOperation {
    @Override
    final void verifyStructure(final NormalizedNode<?, ?> modification, final boolean verifyChildren) {
        delegate().verifyStructure(modification, verifyChildren);
    }
}
