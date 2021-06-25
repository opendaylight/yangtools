/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

/**
 * Test Model Provider designated to load test resources and provide Schema Context for testing of TypeProviderImpl.
 */
public final class TypeProviderModel {
    public static final String TEST_TYPE_PROVIDER_MODULE_NAME = "test-type-provider";

    private TypeProviderModel() {

    }

    public static EffectiveModelContext createTestContext() {
        return YangParserTestUtils.parseYangResources(TypeProviderModel.class, "/base-yang-types.yang",
            "/" + TEST_TYPE_PROVIDER_MODULE_NAME + ".yang", "/test-type-provider-b.yang");
    }
}
