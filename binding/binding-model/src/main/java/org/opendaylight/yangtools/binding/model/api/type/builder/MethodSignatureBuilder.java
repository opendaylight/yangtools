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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
 * The methods as {@link #addAnnotation(AttachedAnnotation)} and
 * {@link #setComment(TypeMemberComment)} can be used as optional because not all methods
 * MUST contain annotation or comment definitions.
 *
 * @see MethodSignature
 */
public final class MethodSignatureBuilder extends TypeMemberBuilder<MethodSignatureBuilder> {
    private List<MethodSignature.Parameter> parameters = List.of();
    private ValueMechanics mechanics = ValueMechanics.NORMAL;
    private boolean isAbstract;
    private boolean isDefault;

    @NonNullByDefault
    public MethodSignatureBuilder(final String name) {
        super(name);
    }

    /**
     * Sets the flag for declaration of method as abstract or non abstract. If the flag {@code isAbstract == true}
     * the instantiated Method Signature MUST have return value for {@link MethodSignature#isAbstract()} also equals to
     * <code>true</code>.
     *
     * @param newIsAbstract is abstract flag
     */
    @NonNullByDefault
    public MethodSignatureBuilder setAbstract(final boolean newIsAbstract) {
        isAbstract = newIsAbstract;
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
     * Adds Parameter into the List of method parameters. Neither the Name or Type of parameter can be {@code null}.
     *
     * <br>
     * In case that any of parameters are defined as <code>null</code> the
     * method SHOULD throw an {@link IllegalArgumentException}
     *
     * @param type Parameter Type
     * @param name Parameter Name
     */
    @NonNullByDefault
    public MethodSignatureBuilder addParameter(final Type type, final String name) {
        parameters = LazyCollections.lazyAdd(parameters, new Parameter(name, type));
        return this;
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

        return new MethodSignatureImpl(getName(), annotations(), getComment(), getAccessModifier(), getReturnType(),
            params, isAbstract, isDefault, mechanics);
    }

    @Override
    MethodSignatureBuilder thisInstance() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), parameters, getReturnType());
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof MethodSignatureBuilder other && Objects.equals(getName(), other.getName())
            && Objects.equals(parameters, other.parameters) && Objects.equals(getReturnType(), other.getReturnType());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("name", getName())
            .add("returnType", getReturnType())
            .add("parameters", parameters)
            .add("annotations", annotations())
            .add("comment", getComment())
            .toString();
    }
}
