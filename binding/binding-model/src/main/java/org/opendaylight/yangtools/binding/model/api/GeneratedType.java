/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A generated type. Every Java interface has to be specified with:
 * <ul>
 * <li><code>package</code> that belongs into</li>
 * <li><code>interface</code> name (with commentary that <b>SHOULD</b> be present to proper define interface and base
 * <i>contracts</i> specified for interface)</li>
 * <li>Each Generated Type can define list of types that Generated Type can implement to extend it's definition
 * (i.e. interface extends list of interfaces or java class implements list of interfaces)</li>
 * <li>Each Generated Type can contain multiple enclosed definitions of Generated Types (i.e. interface can contain N
 * enclosed interface definitions or enclosed classes)</li>
 * <li><code>enum</code> and <code>constant</code> definitions (i.e. each constant definition is by default defined
 * as <code>public static final</code> + type (either primitive or object) and constant name</li>
 * <li><code>method definitions</code> with specified input parameters (with types) and return values</li>
 * </ul>
 *
 * <p>By the definition of the interface constant, enum, enclosed types and method definitions MUST be public, so there
 * is no need to specify the scope of visibility.
 */
public non-sealed interface GeneratedType extends Type {
    /**
     * Returns a string that contains a human-readable textual description of
     * type definition.
     *
     * @return a human-readable textual description of type definition.
     */
    default String getDescription() {
        throw uoe();
    }

    /**
     * Returns a string that is used to specify a textual cross-reference to an
     * external document, either another module that defines related management
     * information, or a document that provides additional information relevant
     * to this definition.
     *
     * @return a textual cross-reference to an external document.
     */
    default String getReference() {
        throw uoe();
    }

    /**
     * Returns the name of the module, in which generated type was specified.
     *
     * @return the name of the module, in which generated type was specified.
     */
    default String getModuleName() {
        throw uoe();
    }

    /**
     * {@return comment string associated with Generated Type}
     */
    @Nullable TypeComment getComment();

    /**
     * {@return List of annotation definitions associated with generated type}
     */
    @NonNull List<AnnotationType> getAnnotations();

    /**
     * {@return {@code true} if The Generated Type is defined as abstract}
     */
    @Deprecated(forRemoval = true)
    default boolean isAbstract() {
        return true;
    }

    /**
     * {@return List of Types that Generated Type will implement}
     */
    @NonNull List<Type> getImplements();

    /**
     * {@return List of enclosing Generated Types}
     */
    @NonNull List<GeneratedType> getEnclosedTypes();

    /**
     * {@return List of Constant definitions associated with Generated Type}
     */
    @NonNull List<Constant> getConstantDefinitions();

    /**
     * Returns List of Method Definitions associated with Generated Type. The list does not contains getters and setters
     * for properties.
     *
     * @return List of Method Definitions associated with Generated Type.
     */
    @NonNull List<MethodSignature> getMethodDefinitions();

    /**
     * {@return List of Properties that are declared for Generated Transfer Object}
     */
    @NonNull List<GeneratedProperty> getProperties();

    /**
     * {@return YANG source definition, or {@code null} when unavailable}
     */
    default @Nullable YangSourceDefinition yangSourceDefinition() {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Not available at runtime");
    }
}
