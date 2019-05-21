/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier;

/**
 * RFC7952 metadata counterpart to a {@link OpaqueDataNode}. This interface is meant to be used as a companion to
 * a OpaqueDataNode instance, children may be inquired through {@link #getChildren()}.
 *
 * @author Robert Varga
 */
@Beta
public interface OpaqueMetadata extends Identifiable<OpaqueIdentifier>, Immutable {
    /**
     * Return the set of annotations defined in this metadata node. Values must be {@link String}s or
     * effectively-immutable {@link Number}s. The map must also be effectively-immutable.
     *
     * @return The set of annotations attached to the corresponding data node.
     */
    @NonNull Map<QName, Object> getAnnotations();

    /**
     * Returns child nodes. Default implementation returns an empty immutable map.
     *
     * @return Child metadata nodes.
     */
    default @NonNull Map<OpaqueIdentifier, OpaqueMetadata> getChildren() {
        return ImmutableMap.of();
    }
}
