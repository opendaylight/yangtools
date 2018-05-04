/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
@Value.Immutable
public interface YangVariableReferenceExpr extends YangExpr {

    QName getName();
}
