/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import org.junit.jupiter.api.Test;

public class Bug4504Test extends AbstractYinExportTest {
    @Test
    public void test() throws Exception {
        exportYinModules("/bugs/bug4504", null);
    }
}
