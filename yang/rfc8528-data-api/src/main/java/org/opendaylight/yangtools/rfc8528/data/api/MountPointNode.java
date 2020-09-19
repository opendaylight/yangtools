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

/**
 * Common NormalizedNode representation of a YANG mount point.
 *
 * <p>
 * These nodes are not meant to be stored in a DataTree and most NormalizedNode utilities will be confused when
 * they see them. The purpose of this interface is making data interchange between mount point-aware components more
 * seamless.
 */
/*
 * FIXME: 7.0.0: The above is not quite right. DataTree instances should be able to handle mount points and correctly
 *               handle them, provided they get enough support from MountPointContext.
 */
@Beta
public interface MountPointNode extends MixinNode, DataContainerNode<MountPointIdentifier>,
        DataContainerChild<MountPointIdentifier, Collection<DataContainerChild<? extends PathArgument, ?>>> {
    @Override
    default QName getNodeType() {
        return getIdentifier().getLabel();
    }

    /**
     * Return the underlying mount point context.
     *
     * @return Underlying mount point context
     */
    @NonNull MountPointContext getMountPointContext();

    /*
     * FIXME: consider whether this interface should contain some information based on 'parent-reference':
     *        - List<YangXPathExpression.QualifiedBound> getParentReference()
     *        - the node-set required to maintain referential integrity in the subtree of this node
     */
}
