/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.BooleanExpression;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface IfFeatureStatementRfc7950 extends DeclaredStatement<BooleanExpression> {

    @Nonnull BooleanExpression getBooleanExpression();
}
