/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import org.junit.jupiter.api.Test;

class Bug2444Test extends AbstractYinExportTest {
    @Test
    void test() throws Exception {
        exportYinModules("/bugs/bug2444/yang", "/bugs/bug2444/yin");
    }
}
