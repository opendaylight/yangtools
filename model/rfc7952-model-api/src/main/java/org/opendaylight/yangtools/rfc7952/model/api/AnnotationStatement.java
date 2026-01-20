/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentedDeclaredStatement.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'annotation' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc7952">RFC7952</a>.
 */
@NonNullByDefault
public interface AnnotationStatement extends UnknownStatement<AnnotationName>, WithStatus<AnnotationName>,
        IfFeatureAwareDeclaredStatement<AnnotationName>, TypeAwareDeclaredStatement<AnnotationName> {
    /**
     * The definition of {@code nc:get-filter-element-attributes} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.of(MetadataConstants.RFC7952_MODULE, "annotation", "name",
        false, AnnotationStatement.class, AnnotationEffectiveStatement.class);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }

    @Override
    default @NonNull String rawArgument() {
        return argument().qname().getLocalName();
    }
}
