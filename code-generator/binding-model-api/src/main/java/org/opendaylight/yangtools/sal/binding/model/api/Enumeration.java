/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api;

import java.util.List;

import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * Interface provide methods for reading data of enumeration class.
 */
public interface Enumeration extends GeneratedType {

    /**
     *
     * Returns list of annotation definitions associated with enumeration type.
     *
     * @return list of annotation definitions associated with enumeration type.
     *
     */
    @Override
    List<AnnotationType> getAnnotations();

    @Override
    Type getParentType();

    /**
     * Returns list of the couples - name and value.
     *
     * @return list of the enumeration pairs.
     */
    List<Pair> getValues();

    /**
     * Formats enumeration according to rules of the programming language.
     *
     * @return string with source code in some programming language
     */
    String toFormattedString();

    /**
     * Interface is used for reading enumeration item. It means item's name and
     * its value.
     */
    interface Pair extends DocumentedNode {

        /**
         * Returns the name of the enumeration item.
         *
         * @return the name of the enumeration item.
         */
        String getName();

        /**
         * Returns value of the enumeration item.
         *
         * @return the value of the enumeration item.
         */
        Integer getValue();
    }
}
