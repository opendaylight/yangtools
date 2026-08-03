/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Central mapping of types.
 */
// FIXME: much of these are exposed here just because of TYPE_CACHE and should be moved to model.spi
public final class Types {
    @NonNullByDefault
    private static final LoadingCache<Class<?>, ConcreteType> TYPE_CACHE = CacheBuilder.newBuilder().weakKeys()
        .build(new CacheLoader<>() {
            @Override
            public ConcreteType load(final Class<?> key) {
                return ConcreteType.ofClass(key);
            }
        });

    public static final @NonNull ConcreteType BOOLEAN = cachedType(Boolean.class);
    public static final @NonNull ConcreteType STRING = cachedType(String.class);
    public static final @NonNull ConcreteType VOID = cachedType(Void.class);

    @Beta
    public static final @NonNull ConcreteType OBJECT = cachedType(Object.class);

    private static final @NonNull ConcreteType LIST_TYPE = cachedType(List.class);
    private static final @NonNull ConcreteType MAP_TYPE = cachedType(Map.class);
    private static final @NonNull ConcreteType SET_TYPE = cachedType(Set.class);
    private static final @NonNull ParameterizedType LIST_TYPE_WILDCARD = ParameterizedType.of(LIST_TYPE);
    private static final @NonNull ParameterizedType SET_TYPE_WILDCARD = ParameterizedType.of(SET_TYPE);

    private Types() {
        // hidden on purpose
    }

    /**
     * Returns an instance of {@link ConcreteType} which represents JAVA <code>java.lang.Void</code> type.
     *
     * @return <code>ConcreteType</code> instance which represents JAVA <code>java.lang.Void</code>
     */
    public static @NonNull ConcreteType voidType() {
        return VOID;
    }

    /**
     * Returns an instance of {@link ConcreteType} which represents {@link Object} type.
     *
     * @return <code>ConcreteType</code> instance which represents {@link Object}
     * @deprecated use {@link #OBJECT}
     */
    @Deprecated(since = "15.1.0", forRemoval = true)
    public static @NonNull ConcreteType objectType() {
        return OBJECT;
    }

    /**
     * Returns an instance of {@link ConcreteType} describing the class.
     *
     * @param cls Class to describe
     * @return Description of class
     * @deprecated Use publicly-accessible constants and methods in this class, {@link BaseYangTypes} or
     *             {@link ConcreteType#ofClass(Class)}.
     */
    @Deprecated(since = "16.0.0", forRemoval = true)
    public static @NonNull ConcreteType typeForClass(final @NonNull Class<?> cls) {
        return cachedType(cls);
    }

    @NonNullByDefault
    static ConcreteType cachedType(final Class<?> cls) {
        return TYPE_CACHE.getUnchecked(requireNonNull(cls));
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link Map}&lt;K,V&gt;.
     *
     * @param keyType Key Type
     * @param valueType Value Type
     * @return Description of generic type instance
     */
    @NonNullByDefault
    public static ParameterizedType mapTypeFor(final Type keyType, final Type valueType) {
        return ParameterizedType.of(MAP_TYPE, keyType, valueType);
    }

    public static boolean isMapType(final ParameterizedType type) {
        return MAP_TYPE.equals(type.getRawType());
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link Set}&lt;V&gt; with concrete type
     * of value.
     *
     * @param valueType Value Type
     * @return Description of generic type instance of Set
     */
    @NonNullByDefault
    public static ParameterizedType setTypeFor(final Type valueType) {
        return ParameterizedType.of(SET_TYPE, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link Set}&lt;?&gt;.
     *
     * @return Description of type instance of Set
     */
    public static @NonNull ParameterizedType setTypeWildcard() {
        return SET_TYPE_WILDCARD;
    }

    public static boolean isSetType(final ParameterizedType type) {
        return SET_TYPE.equals(type.getRawType());
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link List}&lt;V&gt; with concrete type
     * of value.
     *
     * @param valueType Value Type
     * @return Description of type instance of List
     */
    @NonNullByDefault
    public static ParameterizedType listTypeFor(final Type valueType) {
        return ParameterizedType.of(LIST_TYPE, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed {@link List}&lt;?&gt;.
     *
     * @return Description of type instance of List
     */
    public static @NonNull ParameterizedType listTypeWildcard() {
        return LIST_TYPE_WILDCARD;
    }

    public static boolean isListType(final ParameterizedType type) {
        return LIST_TYPE.equals(type.getRawType());
    }
}
