/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.binding.generator.util.AbstractBaseType;
import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.Enumeration;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;

abstract class AbstractGeneratedType extends AbstractBaseType implements GeneratedType {

    private final Type parent;
    private final String comment;
    private final List<AnnotationType> annotations;
    private final List<Type> implementsTypes;
    private final List<Enumeration> enumerations;
    private final List<Constant> constants;
    private final List<MethodSignature> methodSignatures;
    private final List<GeneratedType> enclosedTypes;
    private final List<GeneratedProperty> properties;
    private final boolean isAbstract;

    public AbstractGeneratedType(AbstractGeneratedTypeBuilder<?> builder) {
        super(builder.getPackageName(), builder.getName());
        this.parent = builder.getParent();
        this.comment = builder.getComment();
        this.annotations = toUnmodifiableAnnotations(builder.getAnnotations());
        this.implementsTypes = Collections.unmodifiableList(builder.getImplementsTypes());
        this.constants = Collections.unmodifiableList(builder.getConstants());
        this.enumerations = toUnmodifiableEnumerations(builder.getEnumerations());
        this.methodSignatures = toUnmodifiableMethods(builder.getMethodDefinitions());
        this.enclosedTypes = toUnmodifiableEnclosedTypes(builder.getEnclosedTypes(),
                builder.getEnclosedTransferObjects());
        this.properties = toUnmodifiableProperties(builder.getProperties());
        this.isAbstract = builder.isAbstract();
    }

    public AbstractGeneratedType(final Type parent, final String packageName, final String name, final String comment,
            final List<AnnotationTypeBuilder> annotationBuilders, final boolean isAbstract,
            final List<Type> implementsTypes, final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders, final List<EnumBuilder> enumBuilders,
            final List<Constant> constants, final List<MethodSignatureBuilder> methodBuilders,
            final List<GeneratedPropertyBuilder> propertyBuilders) {
        super(packageName, name);
        this.parent = parent;
        this.comment = comment;
        this.annotations = toUnmodifiableAnnotations(annotationBuilders);
        this.implementsTypes = Collections.unmodifiableList(implementsTypes);
        this.constants = Collections.unmodifiableList(constants);
        this.enumerations = toUnmodifiableEnumerations(enumBuilders);
        this.methodSignatures = toUnmodifiableMethods(methodBuilders);
        this.enclosedTypes = toUnmodifiableEnclosedTypes(enclosedGenTypeBuilders, enclosedGenTOBuilders);
        this.properties = toUnmodifiableProperties(propertyBuilders);
        this.isAbstract = isAbstract;

    }

    private List<GeneratedType> toUnmodifiableEnclosedTypes(final List<GeneratedTypeBuilder> enclosedGenTypeBuilders,
            final List<GeneratedTOBuilder> enclosedGenTOBuilders) {
        final List<GeneratedType> enclosedTypesList = new ArrayList<>();
        for (final GeneratedTypeBuilder builder : enclosedGenTypeBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.toInstance());
            }
        }

        for (final GeneratedTOBuilder builder : enclosedGenTOBuilders) {
            if (builder != null) {
                enclosedTypesList.add(builder.toInstance());
            }
        }
        return enclosedTypesList;
    }

    protected final List<AnnotationType> toUnmodifiableAnnotations(final List<AnnotationTypeBuilder> annotationBuilders) {
        final List<AnnotationType> annotationList = new ArrayList<>();
        for (final AnnotationTypeBuilder builder : annotationBuilders) {
            annotationList.add(builder.toInstance());
        }
        return Collections.unmodifiableList(annotationList);
    }

    protected final List<MethodSignature> toUnmodifiableMethods(List<MethodSignatureBuilder> methodBuilders) {
        final List<MethodSignature> methods = new ArrayList<>();
        for (final MethodSignatureBuilder methodBuilder : methodBuilders) {
            methods.add(methodBuilder.toInstance(this));
        }
        return Collections.unmodifiableList(methods);
    }

    protected final List<Enumeration> toUnmodifiableEnumerations(List<EnumBuilder> enumBuilders) {
        final List<Enumeration> enums = new ArrayList<>();
        for (final EnumBuilder enumBuilder : enumBuilders) {
            enums.add(enumBuilder.toInstance(this));
        }
        return Collections.unmodifiableList(enums);
    }

    protected final List<GeneratedProperty> toUnmodifiableProperties(List<GeneratedPropertyBuilder> methodBuilders) {
        final List<GeneratedProperty> methods = new ArrayList<>();
        for (final GeneratedPropertyBuilder methodBuilder : methodBuilders) {
            methods.add(methodBuilder.toInstance(this));
        }
        return Collections.unmodifiableList(methods);
    }

    @Override
    public final Type getParentType() {
        return parent;
    }

    @Override
    public final String getComment() {
        return comment;
    }

    @Override
    public final List<AnnotationType> getAnnotations() {
        return annotations;
    }

    @Override
    public final boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public final List<Type> getImplements() {
        return implementsTypes;
    }

    @Override
    public final List<GeneratedType> getEnclosedTypes() {
        return enclosedTypes;
    }

    @Override
    public final List<Enumeration> getEnumerations() {
        return enumerations;
    }

    @Override
    public final List<Constant> getConstantDefinitions() {
        return constants;
    }

    @Override
    public final List<MethodSignature> getMethodDefinitions() {
        return methodSignatures;
    }

    @Override
    public final List<GeneratedProperty> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedType [packageName=");
        builder.append(getPackageName());
        builder.append(", name=");
        builder.append(getName());
        if (parent != null) {
            builder.append(", parent=");
            builder.append(parent.getFullyQualifiedName());
        } else {
            builder.append(", parent=null");
        }
        builder.append(", comment=");
        builder.append(comment);
        builder.append(", annotations=");
        builder.append(annotations);
        builder.append(", enclosedTypes=");
        builder.append(enclosedTypes);
        builder.append(", enumerations=");
        builder.append(enumerations);
        builder.append(", constants=");
        builder.append(constants);
        builder.append(", methodSignatures=");
        builder.append(methodSignatures);
        builder.append("]");
        return builder.toString();
    }
}
