/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri.generated.type.builder;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;
import org.opendaylight.mdsal.binding.model.api.type.builder.AnnotationTypeBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.yangtools.util.LazyCollections;

abstract class AbstractTypeMemberBuilder<T extends TypeMemberBuilder<T>> implements TypeMemberBuilder<T> {
    private final String name;
    private Type returnType;
    private List<AnnotationTypeBuilder> annotationBuilders = Collections.emptyList();
    private TypeMemberComment comment;
    private boolean isFinal;
    private boolean isStatic;
    private AccessModifier accessModifier;

    AbstractTypeMemberBuilder(final String name) {
        this.name = name;
    }

    @Override
    public AnnotationTypeBuilder addAnnotation(final JavaTypeName identifier) {
        final AnnotationTypeBuilder builder = new AnnotationTypeBuilderImpl(identifier);
        this.annotationBuilders = LazyCollections.lazyAdd(this.annotationBuilders, builder);
        return builder;
    }

    public Type getReturnType() {
        return this.returnType;
    }

    protected Iterable<AnnotationTypeBuilder> getAnnotationBuilders() {
        return this.annotationBuilders;
    }

    protected TypeMemberComment getComment() {
        return this.comment;
    }

    protected boolean isFinal() {
        return this.isFinal;
    }

    protected boolean isStatic() {
        return this.isStatic;
    }

    @Override
    public AccessModifier getAccessModifier() {
        return this.accessModifier;
    }

    @Override
    public String getName() {
        return this.name;
    }

    protected abstract T thisInstance();

    @Override
    public T setReturnType(final Type newReturnType) {
        checkArgument(newReturnType != null, "Return Type of member cannot be null!");
        this.returnType = newReturnType;
        return thisInstance();
    }

    @Override
    public T setAccessModifier(final AccessModifier modifier) {
        checkArgument(modifier != null, "Access Modifier for member type cannot be null!");
        this.accessModifier = modifier;
        return thisInstance();
    }

    @Override
    public T setComment(final TypeMemberComment newComment) {
        this.comment = newComment;
        return thisInstance();
    }

    @Override
    public T setFinal(final boolean newIsFinal) {
        this.isFinal = newIsFinal;
        return thisInstance();
    }

    @Override
    public T setStatic(final boolean newIsStatic) {
        this.isStatic = newIsStatic;
        return thisInstance();
    }

    protected List<AnnotationType> toAnnotationTypes() {
        final List<AnnotationType> annotations = new ArrayList<>();
        for (final AnnotationTypeBuilder annotBuilder : getAnnotationBuilders()) {
            if (annotBuilder != null) {
                annotations.add(annotBuilder.build());
            }
        }

        return ImmutableList.copyOf(annotations);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getName());
        result = prime * result + Objects.hashCode(getReturnType());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final AbstractTypeMemberBuilder<?> other = (AbstractTypeMemberBuilder<?>) obj;
        return Objects.equals(getName(), other.getName()) && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        return new StringBuilder().append("GeneratedPropertyImpl [name=").append(getName())
                .append(", annotations=").append(getAnnotationBuilders())
                .append(", comment=").append(getComment())
                .append(", returnType=").append(getReturnType())
                .append(", isFinal=").append(isFinal())
                .append(", modifier=").append(getAccessModifier())
                .append(']').toString();
    }
}
