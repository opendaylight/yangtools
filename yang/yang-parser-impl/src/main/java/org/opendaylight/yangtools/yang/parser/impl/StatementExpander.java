/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 *
 * Contributor to parsing process which expands one statement
 * and its children into target node.
 *
 * Such examples are uses nodes and augmentations - which expands their content
 * to effective model of parent or augmentation target.
 *
 * FIXME: Bug 2512: Make this public SPI, once refactor of parser internals
 *        allows for custom expanders, and expanders are not hardwired into
 *        build pipeline.
 *
 * @param <D> Expansion definition
 * @param <T> Target node
 */
interface StatementExpander<D,T> {

    /**
     *
     * @param definition Definition to be expanded
     * @param target Target node, in which nodes will be contributed.
     * @throws YangParseException
     */
    void expand(D definition, T target) throws YangParseException;

}
