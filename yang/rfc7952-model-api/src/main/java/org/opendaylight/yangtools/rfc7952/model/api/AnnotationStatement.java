/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentedDeclaredStatement.WithStatus;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'annotation' extension defined in
 * <a href="https://tools.ietf.org/html/rfc7952">RFC7952</a>.
 */
@Beta
public interface AnnotationStatement extends UnknownStatement<QName>, WithStatus<QName>,
    IfFeatureAwareDeclaredStatement<QName>, TypeAwareDeclaredStatement {

}
