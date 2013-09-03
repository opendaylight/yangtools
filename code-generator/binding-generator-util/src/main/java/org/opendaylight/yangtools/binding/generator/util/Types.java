/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.opendaylight.yangtools.binding.generator.util.generated.type.builder.GeneratedTOBuilderImpl;
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.WildcardType;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;

public final class Types {
    private static final Type SET_TYPE = typeForClass(Set.class);
    private static final Type LIST_TYPE = typeForClass(List.class);
    private static final Type MAP_TYPE = typeForClass(Map.class);
    public static final ConcreteType NUMBER = typeForClass(Number.class);
    public static final ConcreteType BIG_DECIMAL = typeForClass(BigDecimal.class);
    public static final ConcreteType BIG_INTEGER = typeForClass(BigInteger.class);
    public static final ConcreteType BYTE = typeForClass(Byte.class);
    public static final ConcreteType BOOLEAN = typeForClass(Boolean.class);
    public static final ConcreteType DOUBLE = typeForClass(Double.class);
    public static final ConcreteType FLOAT = typeForClass(Float.class);
    public static final ConcreteType INTEGER = typeForClass(Integer.class);
    public static final ConcreteType LONG = typeForClass(Long.class);
    public static final ConcreteType SHORT = typeForClass(Short.class);
    public static final ConcreteType STRING = typeForClass(String.class);
    public static final ConcreteType CHAR_SEQUENCE = typeForClass(CharSequence.class);
    public static final ConcreteType THREAD = typeForClass(Thread.class);
    public static final ConcreteType FUTURE = typeForClass(Future.class);
    public static final ConcreteType CALLABLE = typeForClass(Callable.class);
    public static final ConcreteType VOID = typeForClass(Void.class);
    public static final ConcreteType THROWABLE = typeForClass(Throwable.class);
    public static final ConcreteType EXCEPTION = typeForClass(Exception.class);

    /**
     * It is not desirable to create instance of this class
     */
    private Types() {
    }

    /**
     * Creates the instance of type
     * {@link org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
     * ConcreteType} which represents JAVA <code>void</code> type.
     * 
     * @return <code>ConcreteType</code> instance which represents JAVA
     *         <code>void</code>
     */
    public static ConcreteType voidType() {
        return VOID;
    }

    /**
     * Creates the instance of type
     * {@link org.opendaylight.yangtools.sal.binding.model.api.ConcreteType
     * ConcreteType} which represents primitive JAVA type for which package
     * doesn't exist.
     * 
     * @param primitiveType
     *            string containing programaticall construction based on
     *            primitive type (e.g byte[])
     * @return <code>ConcreteType</code> instance which represents programatic
     *         construction with primitive JAVA type
     */
    public static Type primitiveType(final String primitiveType) {
        return new ConcreteTypeImpl("", primitiveType);
    }

    /**
     * Returns an instance of {@link ConcreteType} describing the class
     * 
     * @param cls
     *            Class to describe
     * @return Description of class
     */
    public static ConcreteType typeForClass(Class<?> cls) {
        return new ConcreteTypeImpl(cls.getPackage().getName(), cls.getSimpleName());
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed
     * {@link Map}<K,V>
     * 
     * @param keyType
     *            Key Type
     * @param valueType
     *            Value Type
     * @return Description of generic type instance
     */
    public static ParameterizedType mapTypeFor(Type keyType, Type valueType) {
        return parameterizedTypeFor(MAP_TYPE, keyType, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed
     * {@link Set}<V> with concrete type of value.
     * 
     * @param valueType
     *            Value Type
     * @return Description of generic type instance of Set
     */
    public static ParameterizedType setTypeFor(Type valueType) {
        return parameterizedTypeFor(SET_TYPE, valueType);
    }

    /**
     * Returns an instance of {@link ParameterizedType} describing the typed
     * {@link List}<V> with concrete type of value.
     * 
     * @param valueType
     *            Value Type
     * @return Description of type instance of List
     */
    public static ParameterizedType listTypeFor(Type valueType) {
        return parameterizedTypeFor(LIST_TYPE, valueType);
    }

    /**
     * Creates generated transfer object for
     * {@link org.opendaylight.yangtools.yang.binding.BaseIdentity BaseIdentity}
     * 
     * @return generated transfer object which is used as extension when YANG
     *         <code>identity</code> is mapped to generated TO
     */
    public static GeneratedTransferObject getBaseIdentityTO() {
        Class<BaseIdentity> cls = BaseIdentity.class;
        GeneratedTOBuilderImpl gto = new GeneratedTOBuilderImpl(cls.getPackage().getName(), cls.getSimpleName());
        return gto.toInstance();
    }

    /**
     * Creates instance of type
     * {@link org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType
     * ParameterizedType}
     * 
     * @param type
     *            JAVA <code>Type</code> for raw type
     * @param parameters
     *            JAVA <code>Type</code>s for actual parameter types
     * @return <code>ParametrizedType</code> reprezentation of <code>type</code>
     *         and its parameters <code>parameters</code>
     */
    public static ParameterizedType parameterizedTypeFor(Type type, Type... parameters) {
        return new ParametrizedTypeImpl(type, parameters);
    }

    /**
     * Creates instance of type
     * {@link org.opendaylight.yangtools.sal.binding.model.api.WildcardType
     * WildcardType}
     * 
     * @param packageName
     *            string with the package name
     * @param typeName
     *            string with the type name
     * @return <code>WildcardType</code> reprezentation of
     *         <code>packageName</code> and <code>typeName</code>
     */
    public static WildcardType wildcardTypeFor(String packageName, String typeName) {
        return new WildcardTypeImpl(packageName, typeName);
    }

    /**
     * Creates instance of
     * {@link org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType
     * ParameterizedType} where raw type is
     * {@link org.opendaylight.yangtools.yang.binding.Augmentable} and actual
     * parameter is <code>valueType</code>.
     * 
     * @param valueType
     *            JAVA <code>Type</code> with actual parameter
     * @return <code>ParametrizedType</code> reprezentation of raw type
     *         <code>Augmentable</code> with actual parameter
     *         <code>valueType</code>
     */
    public static ParameterizedType augmentableTypeFor(Type valueType) {
        final Type augmentable = typeForClass(Augmentable.class);
        return parameterizedTypeFor(augmentable, valueType);
    }

    /**
     * Creates instance of
     * {@link org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType
     * ParameterizedType} where raw type is
     * {@link org.opendaylight.yangtools.yang.binding.Augmentation} and actual
     * parameter is <code>valueType</code>.
     * 
     * @param valueType
     *            JAVA <code>Type</code> with actual parameter
     * @return <code>ParametrizedType</code> reprezentation of raw type
     *         <code>Augmentation</code> with actual parameter
     *         <code>valueType</code>
     */
    public static ParameterizedType augmentationTypeFor(Type valueType) {
        final Type augmentation = typeForClass(Augmentation.class);
        return parameterizedTypeFor(augmentation, valueType);
    }

    /**
     * 
     * Represents concrete JAVA type.
     * 
     */
    private static final class ConcreteTypeImpl extends AbstractBaseType implements ConcreteType {
        /**
         * Creates instance of this class with package <code>pkName</code> and
         * with the type name <code>name</code>.
         * 
         * @param pkName
         *            string with package name
         * @param name
         *            string with the name of the type
         */
        private ConcreteTypeImpl(String pkName, String name) {
            super(pkName, name);
        }
    }

    /**
     * 
     * Represents parametrized JAVA type.
     * 
     */
    private static class ParametrizedTypeImpl extends AbstractBaseType implements ParameterizedType {
        /**
         * Array of JAVA actual type parameters.
         */
        private Type[] actualTypes;

        /**
         * JAVA raw type (like List, Set, Map...)
         */
        private Type rawType;

        @Override
        public Type[] getActualTypeArguments() {

            return actualTypes;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        /**
         * Creates instance of this class with concrete rawType and array of
         * actual parameters.
         * 
         * @param rawType
         *            JAVA <code>Type</code> for raw type
         * @param actTypes
         *            array of actual parameters
         */
        public ParametrizedTypeImpl(Type rawType, Type[] actTypes) {
            super(rawType.getPackageName(), rawType.getName());
            this.rawType = rawType;
            this.actualTypes = actTypes;
        }

    }

    /**
     * 
     * Represents JAVA bounded wildcard type.
     * 
     */
    private static class WildcardTypeImpl extends AbstractBaseType implements WildcardType {
        /**
         * Creates instance of this class with concrete package and type name.
         * 
         * @param packageName
         *            string with the package name
         * @param typeName
         *            string with the name of type
         */
        public WildcardTypeImpl(String packageName, String typeName) {
            super(packageName, typeName);
        }
    }

}
