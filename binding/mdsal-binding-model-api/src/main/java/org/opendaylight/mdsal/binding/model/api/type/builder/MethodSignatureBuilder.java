/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api.type.builder;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.TypeMemberComment;

/**
 * Method Signature Builder serves solely for building Method Signature and
 * returning the <code>new</code> instance of Method Signature. <br>
 * By definition of {@link MethodSignature} the Method in java MUST contain
 * Name, Return Type and Access Modifier. By default the Access Modifier can be
 * set to public. The Method Signature builder does not contain method for
 * addName due to enforce reason that MethodSignatureBuilder SHOULD be
 * instantiated only once with defined method name. <br>
 * The methods as {@link #addAnnotation(String, String)} and
 * {@link #setComment(TypeMemberComment)} can be used as optional because not all methods
 * MUST contain annotation or comment definitions.
 *
 * @see MethodSignature
 */
public interface MethodSignatureBuilder extends TypeMemberBuilder<MethodSignatureBuilder> {
    /**
     * Sets the flag for declaration of method as abstract or non abstract. If the flag {@code isAbstract == true}
     * the instantiated Method Signature MUST have return value for {@link MethodSignature#isAbstract()} also equals to
     * <code>true</code>.
     *
     * @param isAbstract is abstract flag
     */
    MethodSignatureBuilder setAbstract(boolean isAbstract);

    /**
     * Sets the flag indicating whether this is a {@code default interface} method.
     *
     * @param isDefault true if this signature is to represent a default method.
     * @return this builder
     */
    MethodSignatureBuilder setDefault(boolean isDefault);

    @Beta
    MethodSignatureBuilder setMechanics(ValueMechanics mechanics);

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
    MethodSignatureBuilder addParameter(Type type, String name);

    /**
     * Returns <code>new</code> <i>immutable</i> instance of Method Signature. <br>
     * The <code>definingType</code> param cannot be <code>null</code>. Every method in Java MUST be declared and
     * defined inside the scope of <code>class</code> or <code>interface</code> definition. In case that defining Type
     * will be passed as <code>null</code> reference the method SHOULD thrown {@link IllegalArgumentException}.
     *
     * @param definingType Defining Type of Method Signature
     * @return <code>new</code> <i>immutable</i> instance of Method Signature.
     */
    MethodSignature toInstance(Type definingType);
}
