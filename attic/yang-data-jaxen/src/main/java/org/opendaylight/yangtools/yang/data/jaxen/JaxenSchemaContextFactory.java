/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathSchemaContext;
import org.opendaylight.yangtools.yang.data.jaxen.api.XPathSchemaContextFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices
@Singleton
@Component
@NonNullByDefault
public final class JaxenSchemaContextFactory implements XPathSchemaContextFactory {
    private static final Logger LOG = LoggerFactory.getLogger(JaxenSchemaContextFactory.class);

    @Inject
    public JaxenSchemaContextFactory() {
        // For DI
    }

    @Override
    public XPathSchemaContext createContext(final EffectiveModelContext context) {
        return new JaxenSchemaContext(context);
    }

    @Activate
    @SuppressWarnings("static-method")
    void activate() {
        LOG.info("Jaxen XPathSchemaContextFactory enabled");
    }

    @Deactivate
    @SuppressWarnings("static-method")
    void deactivate() {
        LOG.info("Jaxen XPathSchemaContextFactory disabled");
    }
}
