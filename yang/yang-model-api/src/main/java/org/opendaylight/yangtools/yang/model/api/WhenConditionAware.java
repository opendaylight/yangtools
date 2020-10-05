/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Mix-in interface for nodes which can be conditional on a when statement.
 *
 * @author Robert Varga
 */
@Beta
public interface WhenConditionAware {
    /**
     * Returns when statement.
     *
     * <p>
     * If when condition is present node defined by the parent data definition
     * statement is only valid when the returned XPath
     * expression conceptually evaluates to "true"
     * for a particular instance, then the node defined by the parent data
     * definition statement is valid; otherwise, it is not.
     *
     * @return XPath condition
     */
    Optional<? extends QualifiedBound> getWhenCondition();

}
