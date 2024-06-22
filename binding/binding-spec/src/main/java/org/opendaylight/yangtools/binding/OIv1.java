/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

final class OIv1 extends ORv1 {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("redundantModifier")
    public OIv1() {
        // For Externalizable
    }

    OIv1(final DataObjectIdentifier<?> source) {
        super(source);
    }

    @Override
    DataObjectIdentifier<?> resolve(final DataObjectWildcard<?> wildcard) {
        return wildcard.toIdentifier();
    }
}
