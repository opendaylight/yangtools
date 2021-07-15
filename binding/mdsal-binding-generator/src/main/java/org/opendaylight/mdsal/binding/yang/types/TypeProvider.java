/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import org.opendaylight.mdsal.binding.model.api.Restrictions;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

interface TypeProvider {
    /**
     * Resolve of YANG Type Definition to it's java counter part. If the Type Definition contains one of YANG primitive
     * types the method will return {@code java.lang.} counterpart. (For example if YANG type is int32 the Java
     * counterpart is {@link Integer}). In case that Type Definition contains extended type defined via YANG typedef
     * statement the method SHOULD return Generated Type or Generated Transfer Object if that Type is correctly
     * referenced to resolved imported YANG module.
     *
     * <p>
     * The method will return <code>null</code> value in situations that TypeDefinition can't be resolved (either due
     * to missing YANG import or incorrectly specified type).
     *
     * <p>
     * {@code leafref} resolution for relative paths has two models of operation: lenient and strict. This is needed to
     * handle the case where a grouping leaf's path points outside of the grouping tree. In such a case we cannot
     * completely determine the correct type and need to fallback to {@link Object}.
     *
     * @param type Type Definition to resolve from
     * @param lenientRelativeLeafrefs treat relative leafrefs leniently
     * @return Resolved Type
     */
    default Type javaTypeForSchemaDefinitionType(final TypeDefinition<?> type, final SchemaNode parentNode) {
        return javaTypeForSchemaDefinitionType(type, parentNode, null);
    }

    /**
     * Converts schema definition type <code>typeDefinition</code> to JAVA <code>Type</code>.
     *
     * @param typeDefinition type definition which is converted to JAVA type
     * @throws IllegalArgumentException
     *             <ul>
     *             <li>if <code>typeDefinition</code> equal null</li>
     *             <li>if Qname of <code>typeDefinition</code> equal null</li>
     *             <li>if name of <code>typeDefinition</code> equal null</li>
     *             </ul>
     */
    Type javaTypeForSchemaDefinitionType(TypeDefinition<?> type, SchemaNode parentNode, Restrictions restrictions);
}
