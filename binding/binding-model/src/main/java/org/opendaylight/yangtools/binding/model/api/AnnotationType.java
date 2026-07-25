/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;

/**
 * The Annotation Type interface is designed to hold information about annotation for any type that could be annotated
 * in Java.<br>
 * For sake of simplicity the Annotation Type is not designed to model exact behaviour of annotation mechanism,
 * but just to hold information needed to model annotation over java Type definition.
 */
public non-sealed interface AnnotationType extends Type {
    /**
     * Annotation Type parameter interface. For simplicity the Parameter contains values and value types as Strings.
     */
    interface Parameter {
        /**
         * Returns the Name of the parameter.
         *
         * @return the Name of the parameter.
         */
        String getName();

        /**
         * Returns value in String format if Parameter contains singular value, otherwise MAY return <code>null</code>.
         *
         * @return value in String format if Parameter contains singular value.
         */
        String getValue();
    }

    /**
     * Returns Parameter Definition assigned for given parameter name. If Annotation does not contain parameter
     * with specified param name, the method MAY return <code>null</code> value.
     *
     * @param paramName Parameter Name
     * @return Parameter Definition assigned for given parameter name.
     */
    Parameter getParameter(String paramName);

    /**
     * Returns List of all parameters assigned to Annotation Type.
     *
     * @return List of all parameters assigned to Annotation Type.
     */
    List<Parameter> getParameters();

    /**
     * Returns List of parameter names.
     *
     * @return List of parameter names.
     */
    List<String> getParameterNames();
}
