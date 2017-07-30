/*
 * Copyright (c) 2017 Pantheon Technologies, s.ro. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.base.Optional;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

final class AbsentRoot implements NormalizedNodeContainer<PathArgument, PathArgument, NormalizedNode<PathArgument, ?>> {
    private static final AbsentRoot INSTANCE = new AbsentRoot();

    private AbsentRoot() {

    }

    static NormalizedNodeContainer<?, ?, ?> instance() {
        return INSTANCE;
    }

    @Override
    public QName getNodeType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PathArgument getIdentifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<NormalizedNode<PathArgument, ?>> getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<NormalizedNode<PathArgument, ?>> getChild(final PathArgument child) {
        throw new UnsupportedOperationException();
    }
}
