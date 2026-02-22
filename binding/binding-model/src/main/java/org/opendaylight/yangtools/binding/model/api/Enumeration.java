/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * Interface provide methods for reading data of enumeration class.
 */
public interface Enumeration extends GeneratedType {
    /**
     * Returns list of the couples - name and value.
     *
     * @return list of the enumeration pairs.
     */
    List<Pair> getValues();

    @Override
    default boolean isAbstract() {
        return false;
    }

    @Override
    default List<Type> getImplements() {
        return List.of();
    }

    @Override
    default List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    default List<Enumeration> getEnumerations() {
        return List.of();
    }

    @Override
    default List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    default List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    default List<GeneratedProperty> getProperties() {
        return List.of();
    }

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
