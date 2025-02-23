/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netconf.databind;

import static java.util.Objects.requireNonNull;

import org.opendaylight.netconf.databind.DatabindPath.Data;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

/**
 * The contents of a {@code error-path} element as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#page-83">RFC8040 errors grouping</a>.
 *
 * @param databind the {@link DatabindContext} to which this path is bound
 * @param path the {@link YangInstanceIdentifier}, {@link YangInstanceIdentifier#empty()} denotes the data root
 */
public record ErrorPath(DatabindContext databind, YangInstanceIdentifier path) {
    public ErrorPath {
        requireNonNull(databind);
        requireNonNull(path);
    }

    public ErrorPath(final Data path) {
        this(path.databind(), path.instance());
    }
}