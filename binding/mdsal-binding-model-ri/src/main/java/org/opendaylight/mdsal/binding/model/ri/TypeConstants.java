/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.ri;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.ScalarTypeObject;

/**
 * Contains constants used in relations with <code>Type</code>.
 */
public final class TypeConstants {

    /**
     * Name or prefix (multiple patterns in builder class as composed with '_'
     * and upper case of the field name) of the class constant which holds the map
     * of regular expressions that need to be enforced on the string value.
     * The map is keyed by Pattern-compatible string and values are XSD-compatible
     * strings.
     */
    public static final @NonNull String PATTERN_CONSTANT_NAME = "PATTERN_CONSTANTS";

    /**
     * Name of the property holding the value encapsulated in a {@link ScalarTypeObject}.
     */
    public static final @NonNull String VALUE_PROP = "value";

    /**
     * Creation of new instance is prohibited.
     */
    private TypeConstants() {
    }
}
