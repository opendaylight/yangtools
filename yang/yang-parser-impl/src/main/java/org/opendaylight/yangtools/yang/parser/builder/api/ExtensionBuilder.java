/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;

/**
 * Builder for extension statement.
 *
 */
public interface ExtensionBuilder extends SchemaNodeBuilder {

    /**
     * Sets argument name as was defined in YANG source
     *
     * @param argument argument name
     */
    void setArgument(String argument);

    /**
     * Sets if extension should be represented in YIN format as element.
     *
     *
     * @param yin true if extension should be represented in YIN as element.
     */
    void setYinElement(boolean yin);

    /**
     *
     * Builds definition of extednsion
     *
     */
    @Override
    ExtensionDefinition build();

}