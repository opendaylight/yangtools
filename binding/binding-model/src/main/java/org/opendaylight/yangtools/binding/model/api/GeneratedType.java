/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import java.util.Optional;

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
public interface GeneratedType extends Type, DocumentedType {
    /**
     * Returns comment string associated with Generated Type.
     *
     * @return comment string associated with Generated Type.
     */
    TypeComment getComment();

    /**
     * Returns List of annotation definitions associated with generated type.
     *
     * @return List of annotation definitions associated with generated type.
     */
    List<AnnotationType> getAnnotations();

    /**
     * Returns <code>true</code> if The Generated Type is defined as abstract.
     *
     * @return <code>true</code> if The Generated Type is defined as abstract.
     */
    boolean isAbstract();

    /**
     * Returns List of Types that Generated Type will implement.
     *
     * @return List of Types that Generated Type will implement.
     */
    List<Type> getImplements();

    /**
     * Returns List of enclosing Generated Types.
     *
     * @return List of enclosing Generated Types.
     */
    List<GeneratedType> getEnclosedTypes();

    /**
     * Returns List of all Enumerator definitions associated with Generated Type.
     *
     * @return List of all Enumerator definitions associated with Generated Type.
     */
    List<Enumeration> getEnumerations();

    /**
     * Returns List of Constant definitions associated with Generated Type.
     *
     * @return List of Constant definitions associated with Generated Type.
     */
    List<Constant> getConstantDefinitions();

    /**
     * Returns List of Method Definitions associated with Generated Type. The list does not contains getters and setters
     * for properties.
     *
     * @return List of Method Definitions associated with Generated Type.
     */
    List<MethodSignature> getMethodDefinitions();

    /**
     * Returns List of Properties that are declared for Generated Transfer Object.
     *
     * @return List of Properties that are declared for Generated Transfer Object.
     */
    List<GeneratedProperty> getProperties();

    /**
     * Returns the YANG definition of this type, if available.
     *
     * @return YANG source definition, or empty when unavailable.
     */
    Optional<YangSourceDefinition> getYangSourceDefinition();
}
