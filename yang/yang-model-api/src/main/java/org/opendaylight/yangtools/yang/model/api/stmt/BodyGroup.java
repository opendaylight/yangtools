/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;


@Rfc6020AbnfRule("body-stmts")
public interface BodyGroup extends DataDefinitionContainer.WithReusableDefinitions {

    Iterable<? extends ExtensionStatement> getExtensions();

    Iterable<? extends FeatureStatement> getFeatures();

    Iterable<? extends IdentityStatement> getIdentities();

    Iterable<? extends AugmentStatement> getAugments();

    Iterable<? extends RpcStatement> getRpcs();

    Iterable<? extends NotificationStatement> getNotifications();

    Iterable<? extends DeviationStatement> getDeviations();

}
