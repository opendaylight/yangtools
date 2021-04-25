/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Model of a RFC7950 XPath Expression. This model is namespace-bound to a particular set of models as conveyed
 * by their {@link org.opendaylight.yangtools.yang.common.QNameModule}s. Function names are bound either to their
 * defining {@link org.opendaylight.yangtools.yang.common.QName}, unprefixed functions are bound to
 * {@link org.opendaylight.yangtools.yang.common.YangConstants#RFC6020_YIN_MODULE} the same way unprefixed statements
 * are.
 *
 * <p>
 * The model supports multiple number storage and math operations -- with IEEE754 being the default as per XPath 1.0
 * specifications, but additional exact operations being available.
 *
 * @author Robert Varga
 */
@NonNullByDefault
package org.opendaylight.yangtools.yang.xpath.api;

import org.eclipse.jdt.annotation.NonNullByDefault;