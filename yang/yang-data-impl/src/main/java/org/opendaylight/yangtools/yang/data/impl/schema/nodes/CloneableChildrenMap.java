/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 * Interface implemented by maps which allow efficient duplication. This interface IS NOT part of the
 * general API contract and is <strong>an internal implementation detail</strong>. It is subject to
 * change and/or removal at any time.
 *
 * @deprecated Deprecated during Beryllium release cycle, scheduled for removal.
 */
@Beta
@Deprecated
public abstract class CloneableChildrenMap implements CloneableMap<PathArgument, DataContainerChild<? extends PathArgument, ?>> {
    CloneableChildrenMap() {
        // Hidden to prevent outside instantiation
    }
}
