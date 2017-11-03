/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'annotation' extension, as defined in
 * <a href="https://tools.ietf.org/html/rfc7952">RFC7952</a>, being attached to a SchemaNode.
 */
@Beta
public interface AnnotationSchemaNode extends UnknownSchemaNode, TypedSchemaNode {
    /**
     * Find all annotations supported by a SchemaContext
     *
     * @param parent Parent to search
     * @return {@link AnnotationSchemaNode}s supported by the SchemaContext..
     */
    static Map<QName, AnnotationSchemaNode> findAll(final SchemaContext context) {
        final Builder<QName, AnnotationSchemaNode> builder = ImmutableMap.builder();
        for (Module module : context.getModules()) {
            for (UnknownSchemaNode node : module.getUnknownSchemaNodes()) {
                if (node instanceof AnnotationSchemaNode) {
                    final AnnotationSchemaNode annotation = (AnnotationSchemaNode) node;
                    builder.put(annotation.getQName(), annotation);
                }
            }
        }

        return builder.build();
    }
}
