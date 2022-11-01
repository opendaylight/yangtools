/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.plugin.generator.api;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;

/**
 * Abstract base class for {@link FileGeneratorFactory} implementations. Its constructor enforces no spaces in
 * identifier.
 */
@NonNullByDefault
public abstract class AbstractFileGeneratorFactory extends AbstractSimpleIdentifiable<String>
        implements FileGeneratorFactory {
    protected AbstractFileGeneratorFactory(final String identifier) {
        super(identifier);
        checkArgument(identifier.indexOf(' ') == -1, "Identifier may not contain any spaces");
    }
}
