/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A piece of data normalized to a particular {@link EffectiveModelContext}. We are making a distinction between
 * {@code data} and {@code metadata} attached to it. This interface represents the former. There are two specializations
 * to this interface:
 * <ul>
 *   <li>{@link NormalizedTree}, which is essentially a moral equivalent of a {@code W3C DOM Document}</li>
 *   <li>{@link NormalizedNode}, which represents any interior node of a {@link NormalizedTree}. In essence it is
 *       equivalent to a {@code W3C DOM Element}.
 * </ul>
 * The
 */
public sealed interface NormalizedData permits NormalizedNode, NormalizedTree {

}
