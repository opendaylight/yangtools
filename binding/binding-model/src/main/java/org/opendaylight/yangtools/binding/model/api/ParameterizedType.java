/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents an instance of simple parameterized type such as List&lt;String&gt;. The parameterized Type is designed
 * to be used to store information of Java  Generic Type. The array of {@link #getActualTypeArguments()} holds
 * information of all generic parameters defined for Parameterized Type.
 */
@NonNullByDefault
public sealed interface ParameterizedType extends Type
        permits ParametrizedType0, ParametrizedType1, ParametrizedType2, ParametrizedTypeN {
    @Override
    default JavaTypeName name() {
        return getRawType().name();
    }

    /**
     * {@return list of Types that are defined for Parameterized Type, empty indicates {@code Foo<>}}
     * (for example if ParameterizedType encapsulates java generic Map that specifies two parameters Map&lt;K,V&gt;
     * and the K is java.lang.Integer and V is defined as GeneratedType the array will contain two Types to store
     * the information of generic parameters.)
     */
    // FIXME: rename to typeArguments()
    List<Type> getActualTypeArguments();

    /**
     * {@return the Raw Type definition of Parameterized Type}}
     */
    // FIXME: rename to rawType()
    Type getRawType();

    /**
     * {@return a new instance parameterized as {@code <?>}}
     * @param rawType the {@link Type} that is being parameterized
     */
    static ParameterizedType of(final Type rawType) {
        return new ParametrizedType0(rawType);
    }

    /**
     * {@return a new instance parameterized with a single argument}
     * @param rawType the {@link Type} that is being parameterized
     * @param arg the single argument
     */
    static ParameterizedType of(final Type rawType, final Type arg) {
        return new ParametrizedType1(rawType, arg);
    }

    /**
     * {@return a new instance parameterized with two arguments}
     * @param rawType the {@link Type} that is being parameterized
     * @param firstArg the first argument
     * @param secondArg the second argument
     */
    static ParameterizedType of(final Type rawType, final Type firstArg, final Type secondArg) {
        return new ParametrizedType2(rawType, firstArg, secondArg);
    }

    /**
     * {@return a new instance parameterized with two or more arguments}
     * @param rawType the {@link Type} that is being parameterized
     * @param firstArg the first argument
     * @param secondArg the second argument
     * @param others other arguments
     */
    static ParameterizedType of(final Type rawType, final Type firstArg, final Type secondArg, final Type... others) {
        return of(rawType, Stream.concat(Stream.of(firstArg, secondArg), Arrays.stream(others))
            .map(Objects::requireNonNull)
            .collect(Collectors.toUnmodifiableList()));
    }

    /**
     * {@return a new instance parameterized with specified arguments}
     * @param rawType the {@link Type} that is being parameterized
     * @param args the arguments
     */
    static ParameterizedType of(final Type rawType, final List<Type> args) {
        return switch (args.size()) {
            case 0 -> of(rawType);
            case 1 -> of(rawType, args.getFirst());
            case 2 -> of(rawType, args.getFirst(), args.getLast());
            default -> new ParametrizedTypeN(rawType, args);
        };
    }
}
