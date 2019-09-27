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
    static JSONCodecFactory lhotkaCodecFactory;
    static JSONCodecFactory rfc7951CodecFactory;

    @BeforeClass
    public static void beforeClass() {
        schemaContext = YangParserTestUtils.parseYangResourceDirectory("/complexjson/yang");
        lhotkaCodecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext);
        rfc7951CodecFactory = JSONCodecFactorySupplier.RFC7951.getShared(schemaContext);
    }

    @AfterClass
    public static void afterClass() {
        rfc7951CodecFactory = null;
        lhotkaCodecFactory = null;
        schemaContext = null;
    }
}
