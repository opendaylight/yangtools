/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The Method Signature interface contains simplified meta model for Java interface method definition. Each method MUST
 * be defined by name, return type, parameters Additionally method MAY contain associated annotations and a comment.
 *
 * <p>By contract if method does not contain any comments or annotation definitions the {@link #getComment()} SHOULD
 * rather return empty string and {@link #getAnnotations()} SHOULD rather return empty list than {@code null} values.
 */
// FIXME: rename to InterfaceMethod
// FIXME: specialize getAnnotations() to return AttachedAnnotation.ToMethod
@NonNullByDefault
public interface MethodSignature extends TypeMember {
    /**
     * The Parameter interface is designed to hold the information of method Parameter(s). The parameter is defined by
     * his Name which MUST be unique as java does not allow multiple parameters with same names for one method and Type
     * that is associated with parameter.
     *
     * @param name the parameter name
     * @param type the {@link Type} that is bounded to parameter name
     */
    record Parameter(String name, Type type) {
        public Parameter {
            requireNonNull(name);
            requireNonNull(type);
        }
    }

    /**
     * Method return type mechanics. This is a bit of an escape hatch for various behaviors which are supported by
     * code generation.
     */
    enum ValueMechanics {
        /**
         * Usual mechanics, nothing special is going on.
         */
        NORMAL,
        /**
         * Mechanics signaling that the method should not be returning empty collections, but rather squash tham
         * to null.
         */
        NULLIFY_EMPTY,
        /**
         * Mechanics signaling that the method cannot legally return null. This is primarily useful for getters, where
         * the declaration should end up having {@link NonNull} annotation attached to return type. For setters this
         * indicates the setter should never accept a null value.
         */
        NONNULL,
    }

    /**
     * {@return {@code true} if this method is a {@code default} method, or {@code false} if it is abstract}
     */
    boolean isDefault();

    /**
     * Returns the List of parameters that method declare. If the method does not contain any parameters, the method
     * will return empty List.
     *
     * @return the List of parameters that method declare.
     */
    List<Parameter> getParameters();

    /**
     * {@return the {@link ValueMechanics} associated with this method}
     */
    ValueMechanics getMechanics();
}
