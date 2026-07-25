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

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.AttachedAnnotation;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;

public abstract sealed class TypeMemberBuilder<T extends TypeMemberBuilder<T>> implements AnnotableTypeBuilder
        permits GeneratedPropertyBuilder, MethodSignatureBuilder {
    private final String name;

    private @Nullable ArrayList<AttachedAnnotation> annotations = null;
    private AccessModifier accessModifier;
    private TypeMemberComment comment;
    private Type returnType;

    @NonNullByDefault
    TypeMemberBuilder(final String name) {
        this.name = requireNonNull(name);
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
    public final T addAnnotation(final AttachedAnnotation annotation) {
        annotations = addAnnotation(annotations, annotation);
        return thisInstance();
    }

    @Beta
    public static @Nullable ArrayList<@NonNull AttachedAnnotation> addAnnotation(
            final @Nullable ArrayList<@NonNull AttachedAnnotation> list,
            final @Nullable AttachedAnnotation annotation) {
        if (annotation == null) {
            return list;
        }
        if (list == null) {
            final var ret = new ArrayList<AttachedAnnotation>(2);
            ret.add(annotation);
            return ret;
        }
        if (!annotation.repeatable()) {
            final var type = annotation.type();
            for (var existing : list) {
                if (annotation.equals(existing)) {
                    throw new IllegalArgumentException("Attempt to repeat " + annotation);
                }
                if (type.equals(existing.type())) {
                    throw new IllegalArgumentException("Attempt to repeat " + annotation + " after " + existing);
                }
            }
        }
        list.add(annotation);
        return list;
    }

    @NonNullByDefault
    final List<AttachedAnnotation> annotations() {
        final var local = annotations;
        if (local == null) {
            return List.of();
        }
        return local.size() == 1 ? Collections.singletonList(requireNonNull(local.getFirst())) : List.copyOf(local);
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

    // non-final for MethodSignatureBuilder
    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()
            .add("name", name)
            .add("annotations", annotations)
            .add("comment", comment)
            .add("returnType", returnType)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("modifier", accessModifier);
    }
}
