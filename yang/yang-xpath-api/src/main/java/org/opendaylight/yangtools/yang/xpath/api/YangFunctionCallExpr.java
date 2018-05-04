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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Function call invocation. Function names without a prefix are mapped into {@link YangConstants#RFC6020_YIN_MODULE},
 * as they are required to be mapped into YANG as per RFC7950 definitions.
 *
 * @author Robert Varga
 */
@Beta
@Value.Immutable
public interface YangFunctionCallExpr extends YangExpr {

    QName getName();

    List<YangExpr> getArguments();
}
