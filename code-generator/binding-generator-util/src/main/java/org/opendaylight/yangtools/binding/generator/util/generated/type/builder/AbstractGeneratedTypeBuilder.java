/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.binding.generator.util.AbstractBaseType;
import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.Constant;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.EnumBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

abstract class AbstractGeneratedTypeBuilder<T extends GeneratedTypeBuilderBase<T>> extends AbstractBaseType implements GeneratedTypeBuilderBase<T> {

    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private List<Type> implementsTypes = Collections.emptyList();
    private List<EnumBuilder> enumDefinitions = Collections.emptyList();
    private List<Constant> constants = Collections.emptyList();
    private List<MethodSignatureBuilder> methodDefinitions = Collections.emptyList();
    private final List<GeneratedTypeBuilder> enclosedTypes = Collections.emptyList();
    private List<GeneratedTOBuilder> enclosedTransferObjects = Collections.emptyList();
    private List<GeneratedPropertyBuilder> properties = Collections.emptyList();
    private String comment = "";
    private boolean isAbstract;

    protected AbstractGeneratedTypeBuilder(final String packageName, final String name) {
        super(packageName, name);
    }

    protected String getComment() {
        return comment;
    }

    protected List<AnnotationTypeBuilder> getAnnotations() {
        return annotationBuilders;
    }

    @Override
    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public List<Type> getImplementsTypes() {
        return implementsTypes;
    }

    protected List<EnumBuilder> getEnumerations() {
        return enumDefinitions;
    }

    protected List<Constant> getConstants() {
        return constants;
    }

    @Override
    public List<MethodSignatureBuilder> getMethodDefinitions() {
        return methodDefinitions;
    }

    protected List<GeneratedTypeBuilder> getEnclosedTypes() {
        return enclosedTypes;
    }

    protected List<GeneratedTOBuilder> getEnclosedTransferObjects() {
        return enclosedTransferObjects;
    }

    protected abstract T thisInstance();

    @Override
    public GeneratedTOBuilder addEnclosingTransferObject(final String name) {
        Preconditions.checkArgument(name != null, "Name for Enclosing Generated Transfer Object cannot be null!");
        GeneratedTOBuilder builder = new GeneratedTOBuilderImpl(getFullyQualifiedName(), name);

        enclosedTransferObjects = LazyCollections.lazyAdd(enclosedTransferObjects, builder);
        return builder;
    }

    @Override
    public T addEnclosingTransferObject(final GeneratedTOBuilder genTOBuilder) {
        Preconditions.checkArgument(genTOBuilder != null, "Parameter genTOBuilder cannot be null!");
        enclosedTransferObjects = LazyCollections.lazyAdd(enclosedTransferObjects, genTOBuilder);
        return thisInstance();
    }

    @Override
    public T addComment(final String comment) {
        this.comment = comment;
        return thisInstance();
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        Preconditions.checkArgument(packageName != null, "Package Name for Annotation Type cannot be null!");
        Preconditions.checkArgument(name != null, "Name of Annotation Type cannot be null!");

        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    @Override
    public T setAbstract(final boolean isAbstract) {
        this.isAbstract = isAbstract;
        return thisInstance();
    }

    @Override
    public T addImplementsType(final Type genType) {
        Preconditions.checkArgument(genType != null, "Type cannot be null");
        implementsTypes = LazyCollections.lazyAdd(implementsTypes, genType);
        return thisInstance();
    }

    @Override
    public Constant addConstant(final Type type, final String name, final Object value) {
        Preconditions.checkArgument(type != null, "Returning Type for Constant cannot be null!");
        Preconditions.checkArgument(name != null, "Name of constant cannot be null!");

        final Constant constant = new ConstantImpl(this, type, name, value);
        constants = LazyCollections.lazyAdd(constants, constant);
        return constant;
    }

    @Override
    public EnumBuilder addEnumeration(final String name) {
        Preconditions.checkArgument(name != null, "Name of enumeration cannot be null!");
        final EnumBuilder builder = new EnumerationBuilderImpl(getFullyQualifiedName(), name);
        enumDefinitions = LazyCollections.lazyAdd(enumDefinitions, builder);
        return builder;
    }

    @Override
    public MethodSignatureBuilder addMethod(final String name) {
        Preconditions.checkArgument(name != null, "Name of method cannot be null!");
        final MethodSignatureBuilder builder = new MethodSignatureBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        builder.setAbstract(true);
        methodDefinitions = LazyCollections.lazyAdd(methodDefinitions, builder);
        return builder;
    }

    @Override
    public boolean containsMethod(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (MethodSignatureBuilder methodDefinition : methodDefinitions) {
            if (name.equals(methodDefinition.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GeneratedPropertyBuilder addProperty(final String name) {
        final GeneratedPropertyBuilder builder = new GeneratedPropertyBuilderImpl(name);
        builder.setAccessModifier(AccessModifier.PUBLIC);
        properties = LazyCollections.lazyAdd(properties, builder);
        return builder;
    }

    @Override
    public boolean containsProperty(final String name) {
        Preconditions.checkArgument(name != null, "Parameter name can't be null");
        for (GeneratedPropertyBuilder property : properties) {
            if (name.equals(property.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getPackageName() == null) ? 0 : getPackageName().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractGeneratedTypeBuilder<?> other = (AbstractGeneratedTypeBuilder<?>) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getPackageName() == null) {
            if (other.getPackageName() != null) {
                return false;
            }
        } else if (!getPackageName().equals(other.getPackageName())) {
            return false;
        }
        return true;
    }

    public Type getParent() {
        return null;
    }

    @Override
    public List<GeneratedPropertyBuilder> getProperties() {
        return properties;
    }
}
