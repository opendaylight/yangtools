/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.AttachedAnnotation;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.Parameter;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeMemberComment;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.MethodSignatureImpl;
import org.opendaylight.yangtools.util.LazyCollections;

/**
 * Method Signature Builder serves solely for building Method Signature and
 * returning the <code>new</code> instance of Method Signature. <br>
 * By definition of {@link MethodSignature} the Method in java MUST contain
 * Name, Return Type and Access Modifier. By default the Access Modifier can be
 * set to public. The Method Signature builder does not contain method for
 * addName due to enforce reason that MethodSignatureBuilder SHOULD be
 * instantiated only once with defined method name. <br>
 * The methods as {@link #addAnnotation(AttachedAnnotation.ToMethod)} and
 * {@link #setComment(TypeMemberComment)} can be used as optional because not all methods
 * MUST contain annotation or comment definitions.
 *
 * @see MethodSignature
 */
public final class MethodSignatureBuilder {
    private final @NonNull String name;

    private @Nullable ArrayList<AttachedAnnotation.ToMethod> annotations = null;
    private List<MethodSignature.Parameter> parameters = List.of();
    private ValueMechanics mechanics = ValueMechanics.NORMAL;
    private boolean isDefault = false;
    private TypeMemberComment comment;
    private Type returnType;

    @NonNullByDefault
    public MethodSignatureBuilder(final String name) {
        this.name = requireNonNull(name);
    }

    /**
     * Adds String definition of comment into Method Signature definition. The comment String MUST NOT contain any
     * comment specific chars (i.e. "/**" or "//") just plain String text description.
     *
     * @param newComment Structured comment
     */
    public @NonNull MethodSignatureBuilder setComment(final TypeMemberComment newComment) {
        comment = newComment;
        return this;
    }

    /**
     * Sets the flag indicating whether this is a {@code default interface} method.
     *
     * @param newIsDefault true if this signature is to represent a default method.
     * @return this builder
     */
    @NonNullByDefault
    public MethodSignatureBuilder setDefault(final boolean newIsDefault) {
        isDefault = newIsDefault;
        return this;
    }

    @Beta
    @NonNullByDefault
    public MethodSignatureBuilder setMechanics(final ValueMechanics newMechanics) {
        mechanics = requireNonNull(newMechanics);
        return this;
    }

    /**
     * Adds return Type into Builder definition for Generated Property. The return Type MUST NOT be <code>null</code>,
     * otherwise the method SHOULD throw {@link IllegalArgumentException}
     *
     * @param newReaturnType Return Type of the member
     */
    @NonNullByDefault
    public MethodSignatureBuilder setReturnType(final Type newReaturnType) {
        returnType = requireNonNull(newReaturnType);
        return this;
    }

    /**
     * Adds Parameter into the List of method parameters. Neither the Name or Type of parameter can be {@code null}.
     *
     * <br>
     * In case that any of parameters are defined as <code>null</code> the
     * method SHOULD throw an {@link IllegalArgumentException}
     *
     * @param paramType Parameter Type
     * @param paremName Parameter Name
     */
    @NonNullByDefault
    public MethodSignatureBuilder addParameter(final Type paramType, final String paremName) {
        parameters = LazyCollections.lazyAdd(parameters, new Parameter(paremName, paramType));
        return this;
    }

    /**
     * Add an {@link AttachedAnnotation.ToMethod} to this builder.
     *
     * @param annotation the {@link AttachedAnnotation.ToMethod}, if {@code null} this method does nothing
     * @return this instance
     */
    public @NonNull MethodSignatureBuilder addAnnotation(final AttachedAnnotation.@Nullable ToMethod annotation) {
        annotations = addAnnotation(annotations, annotation);
        return this;
    }

    @Beta
    public static <T extends AttachedAnnotation> @Nullable ArrayList<@NonNull T> addAnnotation(
            final @Nullable ArrayList<@NonNull T> list, final @Nullable T annotation) {
        if (annotation == null) {
            return list;
        }
        if (list == null) {
            final var ret = new ArrayList<T>(2);
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
    private List<AttachedAnnotation.ToMethod> annotations() {
        final var local = annotations;
        if (local == null) {
            return List.of();
        }
        return local.size() == 1 ? Collections.singletonList(requireNonNull(local.getFirst())) : List.copyOf(local);
    }

    /**
     * Returns <code>new</code> <i>immutable</i> instance of Method Signature. <br>
     * The <code>definingType</code> param cannot be <code>null</code>. Every method in Java MUST be declared and
     * defined inside the scope of <code>class</code> or <code>interface</code> definition. In case that defining Type
     * will be passed as <code>null</code> reference the method SHOULD thrown {@link IllegalArgumentException}.
     *
     * @return <code>new</code> <i>immutable</i> instance of Method Signature.
     */
    @NonNullByDefault
    public MethodSignature build() {
        final var paramSize = parameters.size();
        final var params = switch (paramSize) {
            case 0 -> List.<MethodSignature.Parameter>of();
            case 1 -> Collections.singletonList(parameters.getFirst());
            default -> List.copyOf(parameters);
        };

        return new MethodSignatureImpl(name, annotations(), comment, returnType, params, isDefault, mechanics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters, returnType);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof MethodSignatureBuilder other && name.equals(other.name)
            && Objects.equals(parameters, other.parameters) && Objects.equals(returnType, other.returnType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("name", name)
            .add("returnType", returnType)
            .add("parameters", parameters)
            .add("annotations", annotations())
            .add("comment", comment)
            .toString();
    }
}
