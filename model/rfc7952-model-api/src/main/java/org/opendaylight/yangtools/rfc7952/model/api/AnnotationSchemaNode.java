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
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

/**
 * Represents the effect of 'annotation' extension, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc7952">RFC7952</a>, being attached to a SchemaNode.
 */
@Beta
public interface AnnotationSchemaNode extends UnknownSchemaNode, TypeAware {
    /**
     * Find specified annotation if it is supported by the specified EffectiveModelContext.
     *
     * @param context EffectiveModelContext to search
     * @param name Annotation name
     * @return {@link AnnotationSchemaNode} corresponding to specified name, or empty if it is not supported
     *         by the EffectiveModelContext
     * @throws NullPointerException if any of the arguments is null
     */
    static @NonNull Optional<AnnotationSchemaNode> find(final EffectiveModelContext context,
            final AnnotationName name) {
        if (context instanceof AnnotationSchemaNodeAware aware) {
            return aware.findAnnotation(name);
        }

        return context.findModuleStatement(name.qname().getModule())
            .flatMap(module -> module.effectiveSubstatements().stream()
                .filter(AnnotationSchemaNode.class::isInstance)
                .map(AnnotationSchemaNode.class::cast)
                .filter(annotation -> name.equals(annotation.name()))
                .findAny());
    }

    /**
     * Find all annotations supported by a EffectiveModelContext.
     *
     * @param context EffectiveModelContext to search
     * @return {@link AnnotationSchemaNode}s supported by the EffectiveModelContext
     * @throws NullPointerException if context is null
     */
    static @NonNull Map<AnnotationName, AnnotationSchemaNode> findAll(final EffectiveModelContext context) {
        final var builder = ImmutableMap.<AnnotationName, AnnotationSchemaNode>builder();
        for (var module : context.getModuleStatements().values()) {
            for (var annotation : module.filterEffectiveStatements(AnnotationSchemaNode.class)) {
                builder.put(annotation.name(), annotation);
            }
        }
        return builder.build();
    }

    default @NonNull AnnotationName name() {
        return asEffectiveStatement().argument();
    }

    @Override
    default QName getQName() {
        return name().qname();
    }

    @Override
    AnnotationEffectiveStatement asEffectiveStatement();
}
