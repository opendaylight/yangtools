/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
record SubmoduleInfoRef(SourceRef.ToSubmodule ref, SourceInfo.Submodule info)
        implements SourceInfoRef.OfSubmodule {
    SubmoduleInfoRef {
        requireNonNull(ref);
        requireNonNull(info);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SourceInfoRef.OfSubmodule.class).add("ref", ref).toString();
    }
}
