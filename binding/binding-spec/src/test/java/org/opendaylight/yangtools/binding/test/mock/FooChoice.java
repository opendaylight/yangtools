/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.test.mock;

import org.opendaylight.yangtools.binding.ChoiceIn;

/**
 * Example of a {@link ChoiceIn} specialization. Assuming the model
 * {@snippet lang="yang" :
 *    module foo {
 *      choice foo-choice {
 *        case bar-case {
 *          container bar-case-container;
 *        }
 *      }
 *    }
 * }
 * this class represents the {@code choice} and {@link BarCase} represents the {@code case}.
 */
public interface FooChoice extends ChoiceIn<FooData, FooChoice> {
    @Override
    default Class<FooChoice> implementedChoice() {
        return FooChoice.class;
    }
}
