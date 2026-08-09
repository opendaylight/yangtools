/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;

/**
 * Contains constants used in relations with <code>Type</code>.
 */
@NonNullByDefault
final class TypeConstants {
    /**
     * Name or prefix (multiple patterns in builder class as composed with '_' and upper case of the field name) of the
     * class constant which holds the map of regular expressions that need to be enforced on the string value. The map
     * is keyed by Pattern-compatible string and values are XSD-compatible strings.
     */
    static final String PATTERN_CONSTANT_NAME = "PATTERN_CONSTANTS";

    /**
     * Name of the property holding the value encapsulated in a {@link ScalarTypeObject}.
     */
    static final String VALUE_PROP = "value";

    private TypeConstants() {
        // Hidden on purpose
    }
}
