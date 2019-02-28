/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * RFC7952 metadata counterpart to a {@link NormalizedNodeContainer}.
 *
 * @author Robert Varga
 */
@Beta
public interface NormalizedMetadataContainer extends NormalizedMetadata {
    /**
     * Returns child node identified by provided key.
     *
     * @param child Path argument identifying child node
     * @return Optional with child node if child exists, {@link Optional#empty()} if it does not.
     */
    Optional<? extends NormalizedMetadata> getChild(PathArgument child);
}
