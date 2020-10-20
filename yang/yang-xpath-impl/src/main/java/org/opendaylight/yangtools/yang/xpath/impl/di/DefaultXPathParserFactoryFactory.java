/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl.di;

import dagger.Component;
import javax.inject.Singleton;

@Component
@Singleton
public interface DefaultXPathParserFactoryFactory {

    DefaultXPathParserFactory factory();
}
