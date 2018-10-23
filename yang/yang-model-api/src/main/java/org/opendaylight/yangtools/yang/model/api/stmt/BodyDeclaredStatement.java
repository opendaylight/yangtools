/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;

@Beta
public interface BodyDeclaredStatement extends NotificationStatementAwareDeclaredStatement<String>,
        DataDefinitionAwareDeclaredStatement.WithReusableDefinitions<String>, BodyGroup {
    @Override
    default Collection<? extends ExtensionStatement> getExtensions() {
        return declaredSubstatements(ExtensionStatement.class);
    }

    @Override
    default Collection<? extends FeatureStatement> getFeatures() {
        return declaredSubstatements(FeatureStatement.class);
    }

    @Override
    default Collection<? extends IdentityStatement> getIdentities() {
        return declaredSubstatements(IdentityStatement.class);
    }

    @Override
    default Collection<? extends AugmentStatement> getAugments() {
        return declaredSubstatements(AugmentStatement.class);
    }

    @Override
    default Collection<? extends RpcStatement> getRpcs() {
        return declaredSubstatements(RpcStatement.class);
    }

    @Override
    default Collection<? extends DeviationStatement> getDeviations() {
        return declaredSubstatements(DeviationStatement.class);
    }
}
