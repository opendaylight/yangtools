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
import java.net.URI;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

/**
 * RFC7952 metadata counterpart to a {@link NormalizedNode}. This interface is meant to be used as a companion to
 * a NormalizedNode instance, hence it does not support iterating over its structure like it is possible with
 * {@link NormalizedNode#getValue()}. Children may be inquired through {@link #getChildren()}.
 *
 * <p>
 * This model of metadata <em>does not</em> have the RFC7952 restriction on metadata attachment to {@code list}s and
 * {@code leaf-list}s because NormalizedNode data model has {@link LeafSetNode}, {@link MapNode} and
 * {@link UnkeyedListNode} to which metadata can be attached.
 *
 * @author Robert Varga
 */
@Beta
public interface NormalizedMetadata extends Identifiable<PathArgument>, Immutable {
    /**
     * {@link QNameModule} for use with legacy XML attributes.
     * @deprecated The use on this namespace is discouraged and users are strongly encouraged to proper RFC7952 metadata
     *             annotations.
     */
    @Deprecated
    QNameModule LEGACY_ATTRIBUTE_NAMESPACE = QNameModule.create(URI.create("")).intern();

    /**
     * Return the set of annotations defined in this metadata node. Values are expected to be effectively-immutable
     * scalar types, like {@link String}s, {@link Number}s and similar. The map must also be effectively-immutable.
     *
     * <p>
     * Due to backwards compatibility reasons, keys may include QNames with empty URI (as exposed via
     * {@link #LEGACY_ATTRIBUTE_NAMESPACE}) as their QNameModule. These indicate an unqualified XML attribute and their
     * value can be assumed to be a String. Furthermore, this extends to qualified attributes, which uses the proper
     * namespace, but will not bind to a proper module revision -- these need to be reconciled with a particular
     * SchemaContext and are expected to either be fully decoded, or contain a String value. Handling of such
     * annotations is at the discretion of the user encountering it: preferred way of handling is to either filter or
     * normalize them to proper QNames/values when encountered.
     *
     * <p>
     * This caveat will be removed in a future version.
     *
     * @return The set of annotations attached to the corresponding data node.
     */
    @NonNull Map<QName, Object> getAnnotations();

    /**
     * Returns child nodes. Default implementation returns an empty immutable map.
     *
     * @return Child metadata nodes.
     */
    default @NonNull Map<PathArgument, NormalizedMetadata> getChildren() {
        return ImmutableMap.of();
    }
}
