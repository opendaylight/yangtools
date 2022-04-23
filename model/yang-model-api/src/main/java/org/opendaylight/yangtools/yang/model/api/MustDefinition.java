/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.stmt.MustEffectiveStatement;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Contains methods for accessing constraint declaration for valid data in form
 * of XPath expressions.<br>
 * <br>
 * <i>YANG example:<br>
 * <code>must "ifType != 'ethernet' or (ifType = 'ethernet' and ifMTU = 1500)";</code>
 * </i>
 */
public interface MustDefinition extends ConstraintMetaDefinition, EffectiveStatementEquivalent {
    @Override
    MustEffectiveStatement asEffectiveStatement();

    /**
     * Returns XPath expression which contains constraint.
     *
     * @return XPath expression which represents the value of the argument of the <code>must</code> YANG substatement.
     */
    default QualifiedBound getXpath() {
        return asEffectiveStatement().argument();
    }
}
