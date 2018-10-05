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

@Rfc6020AbnfRule("body-stmts")
@Deprecated
public interface BodyGroup extends DataDefinitionContainer.WithReusableDefinitions, NotificationStatementContainer {

    @NonNull Collection<? extends ExtensionStatement> getExtensions();

    @NonNull Collection<? extends FeatureStatement> getFeatures();

    @NonNull Collection<? extends IdentityStatement> getIdentities();

    @NonNull Collection<? extends AugmentStatement> getAugments();

    @NonNull Collection<? extends RpcStatement> getRpcs();

    @NonNull Collection<? extends DeviationStatement> getDeviations();
}
