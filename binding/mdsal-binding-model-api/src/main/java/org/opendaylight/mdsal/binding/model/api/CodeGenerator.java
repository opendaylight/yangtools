/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

/**
 * Transforms virtual data to the concrete code in programming language.
 */
public interface CodeGenerator {
    /**
     * Generates code for <code>type</code>.
     *
     * @param type Input type to be processed
     * @return generated JAVA code
     */
    String generate(Type type);

    /**
     * Checks if the concrete instance of <code>type</code> fit to concrete implementation of this interface (e.g.
     * method return true if in <code>EnumGenerator</code> (which implements this interface) has input parameter of type
     * Enumeration (which is subtype of Type).
     *
     * @param type Input type to be processed
     * @return true if type is acceptable for processing.
     */
    boolean isAcceptable(Type type);

    /**
     * Returns name of <code>type</code> parameter.
     *
     * @param type Input type to be processed
     * @return name of generated unit
     */
    String getUnitName(Type type);
}
