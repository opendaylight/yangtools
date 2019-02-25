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
import org.eclipse.jdt.annotation.NonNull;

@Beta
public interface BodyDeclaredStatement extends NotificationStatementAwareDeclaredStatement<String>,
        DataDefinitionAwareDeclaredStatement.WithReusableDefinitions<String> {
    default @NonNull Collection<? extends ExtensionStatement> getExtensions() {
        return declaredSubstatements(ExtensionStatement.class);
    }

    default @NonNull Collection<? extends FeatureStatement> getFeatures() {
        return declaredSubstatements(FeatureStatement.class);
    }

    default @NonNull Collection<? extends IdentityStatement> getIdentities() {
        return declaredSubstatements(IdentityStatement.class);
    }

    default @NonNull Collection<? extends AugmentStatement> getAugments() {
        return declaredSubstatements(AugmentStatement.class);
    }

    default @NonNull Collection<? extends RpcStatement> getRpcs() {
        return declaredSubstatements(RpcStatement.class);
    }

    default @NonNull Collection<? extends DeviationStatement> getDeviations() {
        return declaredSubstatements(DeviationStatement.class);
    }
}
