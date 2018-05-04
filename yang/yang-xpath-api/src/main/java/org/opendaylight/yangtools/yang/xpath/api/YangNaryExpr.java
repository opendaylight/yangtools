/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.immutables.value.Value;

@Beta
@Value.Immutable
public interface YangNaryExpr extends YangExpr {

    List<YangExpr> getExpressions();

    YangNaryOperator getOperator();
}
