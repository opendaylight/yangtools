/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.sal.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.sal.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

abstract class AbstractTypeMemberBuilder<T extends TypeMemberBuilder<T>> implements TypeMemberBuilder<T> {
    private final String name;
    private Type returnType;
    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private String comment = "";
    private boolean isFinal;
    private boolean isStatic;
    private AccessModifier accessModifier;

    public AbstractTypeMemberBuilder(final String name) {
        this.name = name;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final String packageName, final String name) {
        Preconditions.checkArgument(packageName != null, "Annotation Type cannot have package name null!");
        Preconditions.checkArgument(name != null, "Annotation Type cannot have name as null!");

        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(packageName, name);
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    protected Type getReturnType() {
        return returnType;
    }

    protected Iterable<AnnotationTypeBuilder> getAnnotationBuilders() {
        return annotationBuilders;
    }

    protected String getComment() {
        return comment;
    }

    protected boolean isFinal() {
        return isFinal;
    }

    protected boolean isStatic() {
        return isStatic;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    @Override
    public String getName() {
        return name;
    }

    protected abstract T thisInstance();

    @Override
    public T setReturnType(final Type returnType) {
        Preconditions.checkArgument(returnType != null, "Return Type of member cannot be null!");
        this.returnType = returnType;
        return thisInstance();
    }

    @Override
    public T setAccessModifier(final AccessModifier modifier) {
        Preconditions.checkArgument(modifier != null, "Access Modifier for member type cannot be null!");
        this.accessModifier = modifier;
        return thisInstance();
    }

    @Override
    public T setComment(final String comment) {
        if (comment == null) {
            this.comment = "";
        }
        this.comment = comment;
        return thisInstance();
    }

    @Override
    public T setFinal(final boolean isFinal) {
        this.isFinal = isFinal;
        return thisInstance();
    }

    @Override
    public T setStatic(final boolean isStatic) {
        this.isStatic = isStatic;
        return thisInstance();
    }

    protected List<AnnotationType> toAnnotationTypes() {
        final List<AnnotationType> annotations = new ArrayList<>();
        for (final AnnotationTypeBuilder annotBuilder : getAnnotationBuilders()) {
            if (annotBuilder != null) {
                annotations.add(annotBuilder.toInstance());
            }
        }

        return ImmutableList.copyOf(annotations);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result
                + ((getReturnType() == null) ? 0 : getReturnType().hashCode());
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
        MethodSignatureBuilderImpl other = (MethodSignatureBuilderImpl) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }
        if (getReturnType() == null) {
            if (other.getReturnType() != null) {
                return false;
            }
        } else if (!getReturnType().equals(other.getReturnType())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GeneratedPropertyImpl [name=");
        builder.append(getName());
        builder.append(", annotations=");
        builder.append(getAnnotationBuilders());
        builder.append(", comment=");
        builder.append(getComment());
        builder.append(", returnType=");
        builder.append(getReturnType());
        builder.append(", isFinal=");
        builder.append(isFinal());
        builder.append(", modifier=");
        builder.append(getAccessModifier());
        builder.append("]");
        return builder.toString();
    }
}
