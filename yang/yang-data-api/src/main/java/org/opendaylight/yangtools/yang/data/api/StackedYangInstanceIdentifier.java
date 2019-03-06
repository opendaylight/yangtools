/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import java.io.Externalizable;
import java.util.List;
import org.opendaylight.yangtools.util.AbstractStackedLinearPath;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

final class StackedYangInstanceIdentifier extends AbstractStackedLinearPath<YangInstanceIdentifier, PathArgument,
        FixedYangInstanceIdentifier, StackedYangInstanceIdentifier> implements YangInstanceIdentifier {
    private static final long serialVersionUID = 1L;

    StackedYangInstanceIdentifier(final YangInstanceIdentifier parent, final PathArgument pathArgument,
            final int hash) {
        super(hash, parent, pathArgument);
    }

    @Override
    public YangInstanceIdentifier toOptimized() {
        return createPath(getPathFromRoot());
    }

    @Override
    public Externalizable toExternalizable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String computeToString(final List<PathArgument> pathFromRoot) {
        return AbstractPathArgument.computeToString(pathFromRoot);
    }

    @Override
    protected YangInstanceIdentifier emptyPath() {
        return FixedYangInstanceIdentifier.EMPTY_INSTANCE;
    }

    @Override
    protected YangInstanceIdentifier thisPath() {
        return this;
    }

    @Override
    protected YangInstanceIdentifier createPath(final List<PathArgument> fromRoot) {
        return YangInstanceIdentifier.create(fromRoot);
    }
}
