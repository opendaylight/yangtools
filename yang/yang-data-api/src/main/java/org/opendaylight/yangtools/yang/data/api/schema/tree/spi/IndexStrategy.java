/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface IndexStrategy extends Immutable {
    Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> createIndexesFromData(final NormalizedNode<?, ?> newValue);

    void updateIndexes(Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes, NormalizedNode<?, ?> data);

    void removeFromIndexes(Map<Set<YangInstanceIdentifier>, TreeNodeIndex<?, ?>> treeNodeIndexes, PathArgument id);
}
