/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.spi.validation;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * namespace used for validating whether a node is of some type, e.g. usable target for some operation or has other
 * significant properties
 */
public interface ValidationBundlesNamespace extends
        IdentifierNamespace<ValidationBundlesNamespace.ValidationBundleType, Collection<?>> {

    public enum ValidationBundleType {
        /**
         * whether a node is suitable refine substatement
         */
        SUPPORTED_REFINE_SUBSTATEMENTS,

        /**
         * whether a node is suitable target for refine operation
         */
        SUPPORTED_REFINE_TARGETS,

        /**
         * whether a node is suitable target for augment operation
         */
        SUPPORTED_AUGMENT_TARGETS,

        /**
         * whether a <a href="https://tools.ietf.org/html/rfc6020#section-7.9.2">case shorthand</a> can be created for a
         * node
         */
        SUPPORTED_CASE_SHORTHANDS,

        /**
         * whether a node is data node
         */
        SUPPORTED_DATA_NODES
    }
}
