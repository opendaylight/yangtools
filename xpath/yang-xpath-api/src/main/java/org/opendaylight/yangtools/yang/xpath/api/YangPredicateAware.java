/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;

/**
 * Common interface for {@link YangFilterExpr} and {@link Step}, both of which can contain predicates. Predicates are
 * expressed in terms of {@link YangExpr}.
 */
public interface YangPredicateAware {
    default Set<YangExpr> getPredicates() {
        return ImmutableSet.of();
    }
}
