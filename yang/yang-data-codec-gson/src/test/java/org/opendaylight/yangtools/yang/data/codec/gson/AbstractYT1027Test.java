/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

public abstract class AbstractYT1027Test extends AbstractComplexJsonTest {
    private static final QName Q_NAME = QName.create("test.namespace", "2019-09-27", "test-name");
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, Q_NAME);
    private static final DecimalTypeDefinition DECIMAL_TYPE = BaseTypes.decimalTypeBuilder(SCHEMA_PATH)
            .setFractionDigits(1).build();

    @Test
    public void testDecimal() {
        assertThat(codecFactory().decimalCodec(DECIMAL_TYPE), instanceOf(wrapperClass()));
    }

    @Test
    public void testInt64() {
        assertThat(codecFactory().int64Codec(BaseTypes.int64Type()), instanceOf(wrapperClass()));
    }

    @Test
    public void testUint64() {
        assertThat(codecFactory().uint64Codec(BaseTypes.uint64Type()), instanceOf(wrapperClass()));
    }

    abstract JSONCodecFactory codecFactory();

    abstract Class<?> wrapperClass();
}
