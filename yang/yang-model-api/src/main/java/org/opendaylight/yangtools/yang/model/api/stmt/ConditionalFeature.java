/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

@Rfc6020AbnfRule("*(if-feature-stmt)")
@Deprecated
public interface ConditionalFeature {

    /**
     * Return attached if-feature statements. Metamodel differs here between RFC6020 and RFC7950: some nodes will be
     * returning an empty collection in YANG 1.0 mode.
     *
     * @return collection of if-feature statements
     */
    @NonNull Collection<? extends IfFeatureStatement> getIfFeatures();
}
