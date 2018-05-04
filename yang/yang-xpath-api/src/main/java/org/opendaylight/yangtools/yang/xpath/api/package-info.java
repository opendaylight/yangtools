/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Model of a RFC7950 XPath Expression
 *
 * @author Robert Varga
 */
@NonNullByDefault
@Value.Style(visibility = ImplementationVisibility.PRIVATE, strictBuilder = true)
package org.opendaylight.yangtools.yang.model.api.xpath;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
