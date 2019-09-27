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

public class Lhotka02YT1027Test extends AbstractYT1027Test {
    private static JSONCodecFactory CODEC_FACTORY;

    @BeforeClass
    public static void createFactory() {
        CODEC_FACTORY = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(SCHEMA_CONTEXT);
    }

    @AfterClass
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
        return "{\n"
                + "  \"yt1027:decimal\": 1.1\n"
                + "}";
    }

    @Override
    String expectedInt64() {
        return "{\n"
                + "  \"yt1027:int64\": 2\n"
                + "}";
    }

    @Override
    String expectedUint64() {
        return "{\n"
                + "  \"yt1027:uint64\": 1\n"
                + "}";
    }
}