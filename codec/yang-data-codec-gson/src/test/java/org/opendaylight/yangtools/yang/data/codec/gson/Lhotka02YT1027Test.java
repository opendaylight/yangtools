/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class Lhotka02YT1027Test extends AbstractYT1027Test {
    private static JSONCodecFactory CODEC_FACTORY;

    @BeforeAll
    public static void createFactory() {
        CODEC_FACTORY = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(SCHEMA_CONTEXT);
    }

    @AfterAll
    public static void destroyFactory() {
        CODEC_FACTORY = null;
    }

    @Override
    JSONCodecFactory codecFactory() {
        return CODEC_FACTORY;
    }

    @Override
    Class<?> wrapperClass() {
        return NumberJSONCodec.class;
    }

    @Override
    String expectedDecimal() {
        return UNQUOTED_DECIMAL;
    }

    @Override
    String expectedInt64() {
        return UNQUOTED_INT64;
    }

    @Override
    String expectedUint64() {
        return UNQUOTED_UINT64;
    }
}
