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
 * Namespace used for validating whether a node is of some type, e.g. usable target for some operation or has other
 * significant properties.
 */
public interface ValidationBundlesNamespace extends
        IdentifierNamespace<ValidationBundlesNamespace.ValidationBundleType, Collection<?>> {

    enum ValidationBundleType {
        /**
         * Whether a node is suitable refine substatement.
         */
        SUPPORTED_REFINE_SUBSTATEMENTS,

        /**
         * Whether a node is suitable target for refine operation.
         */
        SUPPORTED_REFINE_TARGETS,

        /**
         * Whether a node is suitable target for augment operation.
         */
        SUPPORTED_AUGMENT_TARGETS,

        /**
         * Whether a <a href="https://tools.ietf.org/html/rfc6020#section-7.9.2">case shorthand</a> can be created for a
         * node.
         */
        SUPPORTED_CASE_SHORTHANDS,

        /**
         * Whether a node is data node.
         */
        SUPPORTED_DATA_NODES
    }
}
