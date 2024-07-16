/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Wraps combination of <code>packageName</code> and <code>name</code> to the object representation.
 */
@NonNullByDefault
public class DefaultType extends AbstractType {
    private final JavaTypeName identifier;

    protected DefaultType(final JavaTypeName identifier) {
        this.identifier = requireNonNull(identifier);
    }

    @Override
    public final JavaTypeName getIdentifier() {
        return identifier;
    }
}
