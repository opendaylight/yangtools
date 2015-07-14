/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types.stmt.parser.retest;

import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Test Model Provider designated to load test resources and provide Schema Context
 * for testing of TypeProviderImpl
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
final class TypeProviderModel {

    public static final String TEST_TYPE_PROVIDER_MODULE_NAME = "test-type-provider";

    private static final String BASE_YANG_TYPES_PATH = "/base-yang-types.yang";
    private static final String TEST_TYPE_PROVIDER_PATH = "/"+TEST_TYPE_PROVIDER_MODULE_NAME+".yang";
    private static final String TEST_TYPE_PROVIDER_B_PATH = "/test-type-provider-b.yang";

    private static InputStream getInputStream(final String resourceName) {
        return TypeProviderModel.class.getResourceAsStream(resourceName);
    }

    private static List<InputStream> provideTestModelStreams() {
        final List<InputStream> arrayList = new ArrayList<>();

        arrayList.add(getInputStream(BASE_YANG_TYPES_PATH));
        arrayList.add(getInputStream(TEST_TYPE_PROVIDER_PATH));
        arrayList.add(getInputStream(TEST_TYPE_PROVIDER_B_PATH));
        return arrayList;
    }

    public static SchemaContext createTestContext() throws SourceException, ReactorException {
        return RetestUtils.parseYangStreams(provideTestModelStreams());
    }
}
