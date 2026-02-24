/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;

/**
 * A generator of a single file.
 */
@NonNullByDefault
sealed interface Generator permits BuilderGenerator, EnumTypeObjectGenerator, InterfaceGenerator, TOGenerator {
    /**
     * {@return the type this generator is bound to}
     */
    GeneratedType type();

    /**
     * {@return generated file content}
     */
    String generate();

    /**
     * {@return name of generated unit}
     */
    String getUnitName();
}
