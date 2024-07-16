/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.opendaylight.yangtools.binding.model.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * Interface provide methods for reading data of enumeration class.
 */
public interface Enumeration extends GeneratedType {
    @Override
    EnumTypeObjectArchetype archetype();

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
     * Interface is used for reading enumeration item. It means item's name and its value.
     */
    interface Pair extends DocumentedNode.WithStatus {
        /**
         * Returns the name of the enumeration item as it is specified in the input YANG.
         *
         * @return the name of the enumeration item as it is specified in the input YANG.
         */
        String getName();

        /**
         * Returns the binding representation for the name of the enumeration item.
         *
         * @return the binding representation for the name of the enumeration item.
         */
        String getMappedName();

        /**
         * Returns value of the enumeration item.
         *
         * @return the value of the enumeration item.
         */
        int getValue();
    }
}
