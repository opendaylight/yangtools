/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface AugmentationCodec<A extends Augmentation<?>> extends DomCodec<A> {


    @Override
    public CompositeNode serialize(ValueWithQName<A> input);

    @Override
    public ValueWithQName<A> deserialize(Node<?> input);

    public QName getAugmentationQName();

    /**
     * Check if this codec was created for augmentation with given target node
     * path.
     *
     * @param path
     *            identifier of augmentation target node
     * @return true, if this codec is generated for augmentation pointing node
     *         with given path, false otherwise
     */
    boolean isAcceptable(InstanceIdentifier<?> path);

}
