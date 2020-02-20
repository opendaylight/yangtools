/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.repo.api.StatementParserMode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Beta
@Component(immediate = true)
// FIXME: merge this with YangParserFactoryImpl once we have OSGi R7, which we really want because field injection is
//        a static analysis pain. It also results in not-obvious classes like this one.
public final class OSGiYangParserFactory implements YangParserFactory {
    private YangXPathParserFactory xpathFactory = null;
    private YangParserFactory delegate = null;

    @Reference
    public void setXPathFactory(final YangXPathParserFactory factory) {
        xpathFactory = verifyNotNull(factory);
    }

    @Activate
    public void activate() {
        delegate = new YangParserFactoryImpl(xpathFactory);
    }

    @Deactivate
    public void deactivate() {
        delegate = null;
    }

    @Override
    public Collection<StatementParserMode> supportedParserModes() {
        return delegate.supportedParserModes();
    }

    @Override
    public YangParser createParser(final StatementParserMode parserMode) {
        return delegate.createParser(parserMode);
    }
}
