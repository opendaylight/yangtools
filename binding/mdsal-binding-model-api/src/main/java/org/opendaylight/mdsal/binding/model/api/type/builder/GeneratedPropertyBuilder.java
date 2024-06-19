/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api.type.builder;

import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;

/**
 * Generated Property Builder is interface that contains methods to build and instantiate Generated Property definition.
 *
 * @see GeneratedProperty
 */
public interface GeneratedPropertyBuilder extends TypeMemberBuilder<GeneratedPropertyBuilder> {

    GeneratedPropertyBuilder setValue(String value);

    /**
     * Sets isReadOnly flag for property. If property is marked as read only it is the same as set property in Java
     * as final.
     *
     * @param isReadOnly Read Only property flag.
     */
    GeneratedPropertyBuilder setReadOnly(boolean isReadOnly);

    /**
     * Returns <code>new</code> <i>immutable</i> instance of Generated Property. <br>
     * The <code>definingType</code> param cannot be <code>null</code>. The
     * every member in Java MUST be declared and defined inside the scope of
     * <code>class</code> definition. In case that defining Type will be passed
     * as <code>null</code> reference the method SHOULD thrown
     * {@link IllegalArgumentException}.
     *
     * @return <code>new</code> <i>immutable</i> instance of Generated Property.
     */
    GeneratedProperty toInstance();
}
