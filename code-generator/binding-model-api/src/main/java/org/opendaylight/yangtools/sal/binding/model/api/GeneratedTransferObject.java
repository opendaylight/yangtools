/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Generated Transfer Object extends {@link GeneratedType} and is designed to
 * represent Java Class. The Generated Transfer Object contains declarations of
 * member fields stored in List of Properties. The Generated Transfer Object can
 * be extended by exactly ONE Generated Transfer Object as Java don't allow
 * multiple inheritance. For retrieval of implementing Generated Types use
 * {@link #getImplements()} method. <br>
 * Every transfer object SHOULD contain equals, hashCode and toString
 * definitions. For this purpose retrieve definitions through
 * {@link #getEqualsIdentifiers ()}, {@link #getHashCodeIdentifiers()} and
 * {@link #getToStringIdentifiers ()}.
 *
 */
public interface GeneratedTransferObject extends GeneratedType {

    GeneratedProperty getSUID();

    /**
     * Returns the extending Generated Transfer Object or <code>null</code> if
     * there is no extending Generated Transfer Object.
     *
     * @return the extending Generated Transfer Object or <code>null</code> if
     *         there is no extending Generated Transfer Object.
     */
    GeneratedTransferObject getSuperType();

    /**
     * Returns List of Properties that are designated to define equality for
     * Generated Transfer Object.
     *
     * @return List of Properties that are designated to define equality for
     *         Generated Transfer Object.
     */
    List<GeneratedProperty> getEqualsIdentifiers();

    /**
     * Returns List of Properties that are designated to define identity for
     * Generated Transfer Object.
     *
     * @return List of Properties that are designated to define identity for
     *         Generated Transfer Object.
     */
    List<GeneratedProperty> getHashCodeIdentifiers();

    /**
     * Returns List of Properties that will be members of toString definition
     * for Generated Transfer Object.
     *
     * @return List of Properties that will be members of toString definition
     *         for Generated Transfer Object.
     */
    List<GeneratedProperty> getToStringIdentifiers();

    boolean isTypedef();

    /**
     * Returns Base type of Java representation of YANG typedef if set, otherwise it returns null
     *
     * @return Base type of Java representation of YANG typedef if set, otherwise it returns null
     */
    TypeDefinition getBaseType();

    /**
     * Return boolean value which describe whether Generated Transfer Object
     * was/wasn't created from union YANG type.
     *
     * @return true value if Generated Transfer Object was created from union
     *         YANG type.
     */
    boolean isUnionType();

    boolean isUnionTypeBuilder();

    Restrictions getRestrictions();
}
