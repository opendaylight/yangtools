/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.Nonnull;

@Rfc6020AbnfRule("*(if-feature-stmt)")
public interface ConditionalFeature {

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementations of
     * BitStatement, EnumStatement, IdentityStatement and RefineStatement which do not allow if-feature statements.
     * These YANG statements have been changed in YANG 1.1 (RFC7950) and can now contain if-feature statements.
     *
     * @return collection of if-feature statements
     */
    @Nonnull default Collection<? extends IfFeatureStatement> getIfFeatures() {
        return ImmutableList.of();
    }
}
