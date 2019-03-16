/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;

/**
 * Interface implemented by {@link XPathExpression}s which can be further optimized for execution efficiency at the
 * expense of additional processing being performed on them. The decision to optimize a particular expression is left
 * to the user's discretion.
 *
 * <p>
 * Implementations supporting profile-driven and similar optimizations which depend on data being gathered during
 * evaluation should not implement this interface, but rather perform these optimizations transparently behind the
 * scenes. That implies the users can expect those optimizations not interfering with the user's ability to evaluate
 * the expression.
 */
@Beta
public interface OptimizableXPathExpression extends XPathExpression {
    /**
     * Perform optimization of this expression. If an implementation supports different levels of optimization, it
     * should return an {@link OptimizableXPathExpression} as a result of progressing optimizations for as long as
     * it determines further processing can result in execution benefits. Note this expression is expected to remain
     * unchanged.
     *
     * @return An optimized version of this expression.
     */
    @Nonnull XPathExpression optimizeExpression();
}
