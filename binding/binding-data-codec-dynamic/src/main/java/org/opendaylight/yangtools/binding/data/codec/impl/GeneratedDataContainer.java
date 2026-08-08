/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;

/**
 * Intermediate DTO for a generated
 */
sealed interface GeneratedDataContainer {

    Class<?> theClass();

    List<AugmentRuntimeType> possibleAugmentations();

    record GeneratedAugmentation(Class<?> theClass) implements GeneratedDataContainer {
        public GeneratedAugmentation {
            requireNonNull(theClass);
        }

        @Override
        public List<AugmentRuntimeType> possibleAugmentations() {
            return List.of();
        }
    }

    record GeneratedAugmentable(
            Class<?> theClass,
            List<AugmentRuntimeType> possibleAugmentations) implements GeneratedDataContainer {
        public GeneratedAugmentable {
            requireNonNull(theClass);
            possibleAugmentations = List.copyOf(possibleAugmentations);
        }
    }
}
