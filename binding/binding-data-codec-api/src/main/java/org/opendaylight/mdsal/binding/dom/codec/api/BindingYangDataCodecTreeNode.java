/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import org.opendaylight.yangtools.yang.binding.YangData;

/**
 * A {@link BindingDataContainerCodecTreeNode} corresponding to a RFC8040 {@code yang-data}
 * {@link BindingYangDataCodec}.
 *
 * @param <T> {@link YangData} type
 */
public interface BindingYangDataCodecTreeNode<T extends YangData<T>>
        extends BindingDataContainerCodecTreeNode<T>, BindingYangDataCodec<T> {
    // just a class hierarchy thing
}
