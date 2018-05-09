/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;

final class InstanceIdentifierLiteralExpr extends YangLiteralExpr {
    private static final long serialVersionUID = 1L;

    private final List<Step> steps;

    InstanceIdentifierLiteralExpr(final String str, final List<Step> steps) {
        super(str);
        this.steps = ImmutableList.copyOf(steps);
    }

    List<Step> getSteps() {
        return steps;
    }
}
