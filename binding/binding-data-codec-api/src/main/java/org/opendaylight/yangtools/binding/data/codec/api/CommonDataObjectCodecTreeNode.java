/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataContainer.Addressable;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Common interface shared between {@link BindingDataObjectCodecTreeNode} and {@link BindingAugmentationCodecTreeNode}.
 * This interface should never be implemented on its own.
 *
 * @param <T> DataObject type
 */
public interface CommonDataObjectCodecTreeNode<T extends Addressable> extends BindingDataContainerCodecTreeNode<T> {
    /**
     * Serializes the instance identifier step for current node.
     *
     * @param step {@link DataObjectStep}, may be null if {@link DataObjectReference} does not have a representation for
     *             current node (e.g. choice or case).
     * @return {@link PathArgument}, may be null if {@link YangInstanceIdentifier} does not have representation for
     *         current node (e.g. case).
     * @throws IllegalArgumentException If supplied {@code arg} is not valid.
     */
    @Beta
    @Nullable PathArgument serializePathArgument(@Nullable DataObjectStep<?> step);

    /**
     * Deserializes {@link PathArgument} for current node.
     *
     * @param arg a {@link PathArgument}, may be null if {@link YangInstanceIdentifier} does not have a representation
     *            for current node (e.g. case).
     * @return {@link DataObjectStep}, may be null if {@link DataObjectReference} does not have a representation for
     *         current node (e.g. choice or case).
     * @throws IllegalArgumentException If supplied {@code arg} is not valid.
     */
    @Beta
    @Nullable DataObjectStep<?> deserializePathArgument(@Nullable PathArgument arg);
}
