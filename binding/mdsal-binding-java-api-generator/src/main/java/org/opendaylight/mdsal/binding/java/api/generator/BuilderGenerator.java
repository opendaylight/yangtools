/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.mdsal.binding.spec.naming.BindingMapping.AUGMENTABLE_AUGMENTATION_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.model.util.ReferencedTypeImpl;
import org.opendaylight.mdsal.binding.model.util.Types;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.util.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Transformator of the data from the virtual form to JAVA programming language. The result source code represent java
 * class. For generation of the source code is used the template written in XTEND language.
 */
public final class BuilderGenerator implements CodeGenerator {
    private static final Comparator<MethodSignature> METHOD_COMPARATOR = new AlphabeticallyTypeMemberComparator<>();
    private static final Type AUGMENTATION_RET_TYPE;

    static {
        final Method m;
        try {
            m = Augmentable.class.getDeclaredMethod(AUGMENTABLE_AUGMENTATION_NAME, Class.class);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }

        AUGMENTATION_RET_TYPE = new ReferencedTypeImpl(JavaTypeName.create(m.getReturnType()));
    }

    /**
     * Passes via list of implemented types in <code>type</code>.
     *
     * @param type JAVA <code>Type</code>
     * @return boolean value which is true if any of implemented types is of the type <code>Augmentable</code>.
     */
    @Override
    public boolean isAcceptable(final Type type) {
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            for (Type t : ((GeneratedType) type).getImplements()) {
                // "rpc" and "grouping" elements do not implement Augmentable
                if (t.getFullyQualifiedName().equals(Augmentable.class.getName())) {
                    return true;
                } else if (t.getFullyQualifiedName().equals(Augmentation.class.getName())) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code is generated according to the template
     * source code template which is written in XTEND language.
     */
    @Override
    public String generate(final Type type) {
        if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
            return templateForType((GeneratedType) type).generate();
        }
        return "";
    }

    @Override
    public String getUnitName(final Type type) {
        return type.getName() + BuilderTemplate.BUILDER;
    }

    @VisibleForTesting
    static BuilderTemplate templateForType(final GeneratedType type) {
        final GeneratedType genType = type;
        final JavaTypeName origName = genType.getIdentifier();

        final Set<MethodSignature> methods = new LinkedHashSet<>();
        final Type augmentType = createMethods(genType, methods);
        final Set<MethodSignature> sortedMethods = ImmutableSortedSet.orderedBy(METHOD_COMPARATOR)
                .addAll(methods).build();

        final GeneratedTypeBuilder builderTypeBuilder = new CodegenGeneratedTypeBuilder(
            origName.createSibling(origName.simpleName() + BuilderTemplate.BUILDER));

        final GeneratedTOBuilder implTypeBuilder = builderTypeBuilder.addEnclosingTransferObject(
            origName.simpleName() + "Impl");
        implTypeBuilder.addImplementsType(genType);

        return new BuilderTemplate(builderTypeBuilder.build(), genType, propertiesFromMethods(sortedMethods),
            augmentType, getKey(genType));
    }

    private static Type getKey(final GeneratedType type) {
        for (MethodSignature m : type.getMethodDefinitions()) {
            if (BindingMapping.IDENTIFIABLE_KEY_NAME.equals(m.getName())) {
                return m.getReturnType();
            }
        }
        return null;
    }

    /**
     * Returns set of method signature instances which contains all the methods of the <code>genType</code>
     * and all the methods of the implemented interfaces.
     *
     * @returns set of method signature instances
     */
    private static ParameterizedType createMethods(final GeneratedType type, final Set<MethodSignature> methods) {
        methods.addAll(type.getMethodDefinitions());
        return collectImplementedMethods(type, methods, type.getImplements());
    }

    /**
     * Adds to the <code>methods</code> set all the methods of the <code>implementedIfcs</code>
     * and recursively their implemented interfaces.
     *
     * @param methods set of method signatures
     * @param implementedIfcs list of implemented interfaces
     */
    private static ParameterizedType collectImplementedMethods(final GeneratedType type,
            final Set<MethodSignature> methods, final List<Type> implementedIfcs) {
        if (implementedIfcs == null || implementedIfcs.isEmpty()) {
            return null;
        }

        ParameterizedType augmentType = null;
        for (Type implementedIfc : implementedIfcs) {
            if (implementedIfc instanceof GeneratedType && !(implementedIfc instanceof GeneratedTransferObject)) {
                final GeneratedType ifc = (GeneratedType) implementedIfc;
                methods.addAll(ifc.getMethodDefinitions());

                final ParameterizedType t = collectImplementedMethods(type, methods, ifc.getImplements());
                if (t != null && augmentType == null) {
                    augmentType = t;
                }
            } else if (Augmentable.class.getName().equals(implementedIfc.getFullyQualifiedName())) {
                augmentType = Types.parameterizedTypeFor(AUGMENTATION_RET_TYPE,
                    new ReferencedTypeImpl(type.getIdentifier()));
            }
        }

        return augmentType;
    }

    /**
     * Creates set of generated property instances from getter <code>methods</code>.
     *
     * @param set of method signature instances which should be transformed to list of properties
     * @return set of generated property instances which represents the getter <code>methods</code>
     */
    private static Set<GeneratedProperty> propertiesFromMethods(final Collection<MethodSignature> methods) {
        if (methods == null || methods.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<GeneratedProperty> result = new LinkedHashSet<>();
        for (MethodSignature m : methods) {
            final GeneratedProperty createdField = propertyFromGetter(m);
            if (createdField != null) {
                result.add(createdField);
            }
        }
        return result;
    }

    /**
     * Creates generated property instance from the getter <code>method</code> name and return type.
     *
     * @param method method signature from which is the method name and return type obtained
     * @return generated property instance for the getter <code>method</code>
     * @throws IllegalArgumentException <ul>
     *  <li>if the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> equals <code>null</code></li>
     *  <li>if the name of the <code>method</code> is empty</li>
     *  <li>if the return type of the <code>method</code> equals <code>null</code></li>
     * </ul>
     */
    private static GeneratedProperty propertyFromGetter(final MethodSignature method) {
        checkArgument(method != null);
        checkArgument(method.getReturnType() != null);
        checkArgument(method.getName() != null);
        checkArgument(!method.getName().isEmpty());
        final String prefix = BindingMapping.getGetterPrefix(Types.BOOLEAN.equals(method.getReturnType()));
        if (!method.getName().startsWith(prefix)) {
            return null;
        }

        final String fieldName = StringExtensions.toFirstLower(method.getName().substring(prefix.length()));
        final GeneratedTOBuilder tmpGenTO = new CodegenGeneratedTOBuilder(JavaTypeName.create("foo", "foo"));
        tmpGenTO.addProperty(fieldName).setReturnType(method.getReturnType());
        return tmpGenTO.build().getProperties().get(0);
    }
}
