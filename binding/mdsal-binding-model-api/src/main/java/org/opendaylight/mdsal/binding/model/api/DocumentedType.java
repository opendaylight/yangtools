/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * Implementing this interface allows an object to hold information which are
 * essential for generating java doc from type definition.
 */
public interface DocumentedType {

    /**
     * Returns a string that contains a human-readable textual description of
     * type definition.
     *
     * @return a human-readable textual description of type definition.
     */
    String getDescription();

    /**
     * Returns a string that is used to specify a textual cross-reference to an
     * external document, either another module that defines related management
     * information, or a document that provides additional information relevant
     * to this definition.
     *
     * @return a textual cross-reference to an external document.
     */
    String getReference();

    /**
     * Returns a list of QNames which represent schema path in schema tree from
     * actual concrete type to the root.
     *
     * @return a schema path in schema tree from actual concrete schema node
     *         identifier to the root.
     */
    Iterable<QName> getSchemaPath();

    /**
     * Returns the name of the module, in which generated type was specified.
     *
     * @return the name of the module, in which generated type was specified.
     */
    String getModuleName();
}
