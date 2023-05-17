/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Representation of legacy {@code org.opendaylight.yangtools.rfc8528.data.api.MountPointIdentifier}.
 */
@Deprecated(since = "11.0.0")
public record LegacyMountPointIdentifier(@NonNull QName qname) implements LegacyPathArgument {
    public LegacyMountPointIdentifier {
        requireNonNull(qname);
    }
}
