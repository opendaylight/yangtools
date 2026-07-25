/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.util.LazyCollections;

public abstract sealed class TypeMemberBuilder<T extends TypeMemberBuilder<T>> implements AnnotableTypeBuilder
        permits GeneratedPropertyBuilder {
    private final String name;

    private Type returnType;
    private List<AnnotationTypeBuilder> annotationBuilders = List.of();
    private TypeMemberComment comment;
    private AccessModifier accessModifier;

    TypeMemberBuilder(final String name) {
        this.name = name;
    }

    /**
     * {@return the name of member}
     */
    final String getName() {
        return name;
    }

    final Type getReturnType() {
        return returnType;
    }

    /**
     * Adds return Type into Builder definition for Generated Property. The return Type MUST NOT be <code>null</code>,
     * otherwise the method SHOULD throw {@link IllegalArgumentException}
     *
     * @param newReaturnType Return Type of the member
     */
    public final @NonNull T setReturnType(final Type newReaturnType) {
        returnType = requireNonNull(newReaturnType);
        return thisInstance();
    }

    final AccessModifier getAccessModifier() {
        return accessModifier;
    }

    /**
     * Sets the access modifier of property.
     *
     * @param newAccessModifier Access Modifier value.
     */
    public final @NonNull T setAccessModifier(final AccessModifier newAccessModifier) {
        accessModifier = requireNonNull(newAccessModifier);
        return thisInstance();
    }

    final TypeMemberComment getComment() {
        return comment;
    }

    /**
     * Adds String definition of comment into Method Signature definition. The comment String MUST NOT contain any
     * comment specific chars (i.e. "/**" or "//") just plain String text description.
     *
     * @param newComment Structured comment
     */
    public final @NonNull T setComment(final TypeMemberComment newComment) {
        comment = newComment;
        return thisInstance();
    }

    @Override
    public final AnnotationTypeBuilder addAnnotation(final JavaTypeName identifier) {
        final var builder = new AnnotationTypeBuilderImpl(identifier);
        annotationBuilders = LazyCollections.lazyAdd(annotationBuilders, builder);
        return builder;
    }

    final List<AnnotationTypeBuilder> getAnnotationBuilders() {
        return annotationBuilders;
    }

    final List<AnnotationType> toAnnotationTypes() {
        final var size = annotationBuilders.size();
        return switch (size) {
            case 0 -> List.of();
            case 1 -> Collections.singletonList(annotationBuilders.getFirst().build());
            case 2 -> List.of(annotationBuilders.getFirst().build(), annotationBuilders.getLast().build());
            default -> {
                final var tmp = new ArrayList<AnnotationType>(size);
                for (var annotBuilder : annotationBuilders) {
                    tmp.add(annotBuilder.build());
                }
                yield List.copyOf(tmp);
            }
        };
    }

    abstract @NonNull T thisInstance();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(returnType);
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
        final var other = (TypeMemberBuilder<?>) obj;
        return Objects.equals(name, other.name) && Objects.equals(returnType, other.returnType);
    }

    @Override
    public String toString() {
        return new StringBuilder().append("TypeMemberBuilder [name=").append(getName())
            .append(", annotations=").append(annotationBuilders)
            .append(", comment=").append(comment)
            .append(", returnType=").append(returnType)
            .append(", modifier=").append(accessModifier)
            .append(']').toString();
    }
}
