/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'annotation' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc7952">RFC7952</a>.
 */
public interface AnnotationStatement extends UnknownStatement<AnnotationName>,
        DescriptionStatement.OptionalIn<AnnotationName>, IfFeatureStatement.MultipleIn<AnnotationName>,
        ReferenceStatement.OptionalIn<AnnotationName>, StatusStatement.OptionalIn<AnnotationName>,
        TypeStatement.OptionalIn<AnnotationName> {
    /**
     * The definition of {@code nc:get-filter-element-attributes} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<AnnotationName, @NonNull AnnotationStatement, @NonNull AnnotationEffectiveStatement>
        DEF = StatementDefinition.of(AnnotationStatement.class, AnnotationEffectiveStatement.class,
            MetadataConstants.RFC7952_MODULE, "annotation", "name");

    @Override
    default StatementDefinition<AnnotationName, ?, ?> statementDefinition() {
        return DEF;
    }

    @Override
    default @NonNull String rawArgument() {
        return argument().qname().getLocalName();
    }
}
