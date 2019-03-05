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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'annotation' extension, as defined in
 * <a href="https://tools.ietf.org/html/rfc7952">RFC7952</a>, being attached to a SchemaNode.
 */
@Beta
public interface AnnotationSchemaNode extends UnknownSchemaNode, TypeAware {
    /**
     * Find specified annotation if it is supported by the specified SchemaContext.
     *
     * @param context SchemaContext to search
     * @param qname Annotation name
     * @return {@link AnnotationSchemaNode} corresponding to specified name, or empty if it is not supported
     *         by the SchemaContext..
     * @throws NullPointerException if any of the arguments is null
     */
    static @NonNull Optional<AnnotationSchemaNode> find(final SchemaContext context, final QName qname) {
        if (context instanceof AnnotationSchemaNodeAware) {
            return ((AnnotationSchemaNodeAware) context).findAnnotation(qname);
        }

        return context.findModule(qname.getModule()).flatMap(module -> {
            return module.getUnknownSchemaNodes().stream()
                    .filter(AnnotationSchemaNode.class::isInstance)
                    .map(AnnotationSchemaNode.class::cast)
                    .filter(annotation -> qname.equals(annotation.getQName())).findAny();
        });
    }

    /**
     * Find all annotations supported by a SchemaContext.
     *
     * @param context SchemaContext to search
     * @return {@link AnnotationSchemaNode}s supported by the SchemaContext..
     * @throws NullPointerException if context is null
     */
    static @NonNull Map<QName, AnnotationSchemaNode> findAll(final SchemaContext context) {
        final Builder<QName, AnnotationSchemaNode> builder = ImmutableMap.builder();
        for (Module module : context.getModules()) {
            for (UnknownSchemaNode node : module.getUnknownSchemaNodes()) {
                if (node instanceof AnnotationSchemaNode) {
                    builder.put(node.getQName(), (AnnotationSchemaNode) node);
                }
            }
        }

        return builder.build();
    }
}
