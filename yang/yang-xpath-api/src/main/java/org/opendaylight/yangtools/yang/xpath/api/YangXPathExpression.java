/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.xpath;

import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.opendaylight.yangtools.concepts.Immutable;

// FIXME: should this include evaluate(EffectiveStatement/SchemaNode) or similar?
@Beta
@Value.Immutable
public interface YangXPathExpression extends Immutable {

    YangExpr getRootExpr();
}
