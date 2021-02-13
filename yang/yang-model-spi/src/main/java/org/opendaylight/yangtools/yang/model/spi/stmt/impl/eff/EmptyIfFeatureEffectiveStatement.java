/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt.impl.eff;

import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureExpr;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;

public final class EmptyIfFeatureEffectiveStatement extends DefaultArgument<IfFeatureExpr, IfFeatureStatement>
        implements IfFeatureEffectiveStatement {
    public EmptyIfFeatureEffectiveStatement(final IfFeatureStatement declared) {
        super(declared);
    }
}
