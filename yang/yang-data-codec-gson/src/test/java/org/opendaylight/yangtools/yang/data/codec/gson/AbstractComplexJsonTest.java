/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public abstract class AbstractComplexJsonTest {

    static SchemaContext schemaContext;
    static LhotkaJSONCodecFactory lhotkaCodecFactory;

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/complexjson/yang");
        lhotkaCodecFactory = LhotkaJSONCodecFactorySupplier.getInstance().getShared(schemaContext);
    }

    @AfterClass
    public static void afterClass() {
        lhotkaCodecFactory = null;
        schemaContext = null;
    }
}
