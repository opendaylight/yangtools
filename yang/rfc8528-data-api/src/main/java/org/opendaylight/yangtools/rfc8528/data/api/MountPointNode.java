/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MixinNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTree;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

/**
 * Common NormalizedNode representation of a YANG mount point. This interface is not meant to be implemented directly,
 * but rather used through its specializations like {@link InlineMountPointNode} and {@link SharedSchemaMountPointNode}.
 *
 * <p>
 * Furthermore, these nodes are not meant to be stored in a {@link DataTree} and most NormalizedNode utilities will be
 * confused when they see them. The purpose of this interface is making data interchange between mount point-aware
 * components more seamless.
 */
@Beta
public interface MountPointNode extends SchemaContextProvider, MixinNode, DataContainerNode<MountPointIdentifier>,
        DataContainerChild<MountPointIdentifier, Collection<DataContainerChild<? extends PathArgument, ?>>> {
    @Override
    default QName getNodeType() {
        return getIdentifier().getLabel();
    }

    @Override
    // FIXME: remove this override when SchemaContextProvider's method has sane semantics.
    @NonNull SchemaContext getSchemaContext();
}
