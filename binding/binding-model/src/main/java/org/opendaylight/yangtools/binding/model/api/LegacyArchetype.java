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
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A legacy {@link Archetype} for an interface class. It needs to be specified with:
 * <ul>
 *   <li>{@code package} that belongs into</li>
 *   <li>{@code interface} name (with commentary that <b>SHOULD</b> be present to proper define interface and base
 *       <i>contracts</i> specified for interface)</li>
 *   <li>Each Generated Type can define list of types that Generated Type can implement to extend it's definition
 *       (i.e. interface extends list of interfaces or java class implements list of interfaces)</li>
 *   <li>Each Generated Type can contain multiple enclosed definitions of Generated Types (i.e. interface can contain N
 *       enclosed interface definitions or enclosed classes)</li>
 *   <li>{@code enum} and {@code constant} definitions (i.e. each constant definition is by default defined as
 *       {@code public static final} + type (either primitive or object) and constant name</li>
 *   <li>{@code method definitions} with specified input parameters (with types) and return values</li>
 * </ul>
 *
 * <p>By the definition of the interface constant, enum, enclosed types and method definitions MUST be public, so there
 * is no need to specify the scope of visibility.
 *
 * @param <S> EffectiveStatement type
 */
public non-sealed interface LegacyArchetype<S extends EffectiveStatement<?, ?>> extends Archetype.WithStatement<S> {
    /**
     * {@return List of annotation definitions associated with generated type}
     */
    @NonNull List<AnnotationType> getAnnotations();

    /**
     * {@return List of Types that Generated Type will implement}
     */
    @NonNull List<Type> getImplements();

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
}
