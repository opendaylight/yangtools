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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionAware;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

/**
 * Effective statement representation of 'annotation' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc7952">RFC7952</a>.
 */
public interface AnnotationEffectiveStatement
        extends UnknownEffectiveStatement<AnnotationName, @NonNull AnnotationStatement>, TypeDefinitionAware {
    @Override
    default StatementDefinition statementDefinition() {
        return AnnotationStatement.DEFINITION;
    }
}
