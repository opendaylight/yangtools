/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Navigable tree representing hierarchy of Binding to Normalized Node codecs. This navigable tree is associated to
 * a concrete set of YANG models, represented by SchemaContext and provides access to subtree specific serialization
 * context.
 */
// TODO: Add more detailed documentation
public interface BindingCodecTree {

    @Nullable <T extends DataObject> BindingDataObjectCodecTreeNode<T> getSubtreeCodec(InstanceIdentifier<T> path);

    @Nullable BindingCodecTreeNode getSubtreeCodec(YangInstanceIdentifier path);

    @Nullable BindingCodecTreeNode getSubtreeCodec(SchemaPath path);

    /**
     * Get the BindingIdentityCodec associated with this tree.
     *
     * @return A BindingIdentityCodec instance.
     */
    @Beta
    @NonNull BindingIdentityCodec getIdentityCodec();
}
