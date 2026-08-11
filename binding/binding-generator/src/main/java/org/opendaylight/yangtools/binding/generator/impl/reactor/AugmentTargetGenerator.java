/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

/**
 * Shared interface between generators that support statements that are valid {@code augment} targets.
 */
sealed interface AugmentTargetGenerator permits AugmentableGenerator, ChoiceGenerator {
    // FIXME: YANGTOOLS-1934: much more from DataContainerGenerator
}
