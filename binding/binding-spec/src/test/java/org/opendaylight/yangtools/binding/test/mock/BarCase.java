/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.test.mock;

import org.opendaylight.yangtools.binding.CaseObject;

public interface BarCase extends CaseObject<FooData, FooChoice, BarCase>, FooChoice {
    @Override
    default Class<BarCase> implementedInterface() {
        return BarCase.class;
    }

    @Override
    default Class<BarCase> implementedCase() {
        return BarCase.class;
    }
}
