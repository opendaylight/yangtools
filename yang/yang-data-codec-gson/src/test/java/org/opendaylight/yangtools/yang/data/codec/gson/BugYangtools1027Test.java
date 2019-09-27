/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

public class BugYangtools1027Test extends AbstractComplexJsonTest {

    private static JSONCodecFactory rfc7951CodecFactory;
    private static final QName Q_NAME = QName.create("test.namespace", "2019-09-27", "test-name");
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, Q_NAME);
    private static DecimalTypeDefinition decimalType;
    private static Int64TypeDefinition int64type;
    private static Uint64TypeDefinition uint64type;

    @BeforeClass
    public static void beforeTestClass() {
        rfc7951CodecFactory = JSONCodecFactorySupplier.RFC7951.getShared(schemaContext);
        int64type = BaseTypes.int64Type();
        uint64type = BaseTypes.uint64Type();
        decimalType = BaseTypes.decimalTypeBuilder(SCHEMA_PATH).setFractionDigits(1).build();
    }

    @AfterClass
    public static void afterTestClass() {
        rfc7951CodecFactory = null;
    }

    @Test
    public void int64Codec_lhotkaTest() {
        final JSONCodec<?> jsonCodec = lhotkaCodecFactory.int64Codec(int64type);
        Assert.assertTrue(jsonCodec instanceof NumberJSONCodec);
    }

    @Test
    public void uint64Codec_rfc7951Test() {
        final JSONCodec<?> jsonCodec = rfc7951CodecFactory.int64Codec(int64type);
        Assert.assertTrue(jsonCodec instanceof QuotedJSONCodec);
    }

    @Test
    public void uint64Codec_lhotkaTest() {
        final JSONCodec<?> jsonCodec = lhotkaCodecFactory.uint64Codec(uint64type);
        Assert.assertTrue(jsonCodec instanceof NumberJSONCodec);
    }

    @Test
    public void decimalCodec_rfc7951Test() {
        final JSONCodec<?> jsonCodec = rfc7951CodecFactory.uint64Codec(uint64type);
        Assert.assertTrue(jsonCodec instanceof QuotedJSONCodec);
    }

    @Test
    public void decimalCodec_lhotkaTest() {
        final JSONCodec<?> jsonCodec = lhotkaCodecFactory.decimalCodec(decimalType);
        Assert.assertTrue(jsonCodec instanceof NumberJSONCodec);
    }

    @Test
    public void int64Codec_rfc7951Test() {
        final JSONCodec<?> jsonCodec = rfc7951CodecFactory.decimalCodec(decimalType);
        Assert.assertTrue(jsonCodec instanceof QuotedJSONCodec);
    }
}