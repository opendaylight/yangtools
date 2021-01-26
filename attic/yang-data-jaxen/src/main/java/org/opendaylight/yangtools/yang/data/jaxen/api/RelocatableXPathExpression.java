/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Interface implemented by {@link XPathExpression}s which can be recompiled to execute more efficiently at a
 * different path than they were originally compiled at. This can result in the expression being
 * moved either up or down in a Schema tree.
 */
@Beta
@Deprecated
public interface RelocatableXPathExpression extends XPathExpression {
    /**
     * Return a new XPathExpression relocated to a SchemaPath of the implementation's choosing.
     *
     * @return A new XPathExpression instance.
     */
    @NonNull XPathExpression relocateExpression();
}
