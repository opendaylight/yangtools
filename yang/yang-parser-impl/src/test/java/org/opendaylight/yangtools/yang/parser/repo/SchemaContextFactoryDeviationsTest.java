/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.repo;

import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

public class SchemaContextFactoryDeviationsTest {
    private static SettableSchemaProvider<ASTSchemaSource> getImmediateYangSourceProviderFromResource(
            final String resourceName) throws Exception {
        final YangTextSchemaSource yangSource = YangTextSchemaSource.forResource(resourceName);
        return SettableSchemaProvider.createImmediate(TextToASTTransformer.transformText(yangSource),
                ASTSchemaSource.class);
    }
}
