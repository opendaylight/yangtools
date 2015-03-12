/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 *
 * Codec providing serialization and deserializiation between Binding
 * and NormalizedNode representation of data.
 *
 *
 * @param <T> Binding representation of data
 */
@Beta
public interface BindingNormalizedNodeCodec<T extends DataObject> {

    /**
     * Converts from Normalized Node to Binding representation of data.
     *
     * @param data Normalized Node representation of data
     * @return Binding representation of data
     */
    @Nonnull T deserialize(@Nonnull NormalizedNode<?,?> data);

    /**
     * Converts from  Binding to Normalized Node representation of data.
     *
     * @param data Binding representation of data
     * @return Normalized Node representation of data
     */
    @Nonnull NormalizedNode<?,?> serialize(@Nonnull T data);

}
