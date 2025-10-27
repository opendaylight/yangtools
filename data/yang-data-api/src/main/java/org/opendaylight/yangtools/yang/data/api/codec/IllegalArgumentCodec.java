/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility interface for translation between a external form and an internal form. Implementations should consider
 * subclassing {@link AbstractIllegalArgumentCodec}.
 *
 * @param <S> Serialized (external) type
 * @param <D> Deserialized (internal) type
 */
@Beta
// FIXME: This interface ignores a number of complications when dealing with external forms. For one, it assumes
//        a serdes operation does not have further context than the input -- and this is seldom the case. The other
//        failing is that it actively discourages use of checked exceptions to deal with errors at the appropriate
//        level. Based on these, this interface is deprecated for removal without a replacement. Users are
//        encouraged to define similar interface fitting their needs.
public sealed interface IllegalArgumentCodec<S, D>
        permits AbstractIllegalArgumentCodec, BinaryCodec, BitsCodec, BooleanCodec, DecimalCodec, EmptyCodec, EnumCodec,
                IdentityrefCodec, InstanceIdentifierCodec, Int8Codec, Int16Codec, Int32Codec, Int64Codec, LeafrefCodec,
                StringCodec, Uint8Codec, Uint16Codec, Uint32Codec, Uint64Codec, UnionCodec {
    /**
     * Produce an internal object based on an external object.
     *
     * @param input Input object
     * @return Product derived from input
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException when input is not valid
     */
    @NonNull D deserialize(@NonNull S input);

    /**
     * Produce an external object based on an internal object.
     *
     * @param input Input
     * @return An external form object
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException when input is not valid
     */
    @NonNull S serialize(@NonNull D input);
}
