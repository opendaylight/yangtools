/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.pipeline;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

/**
 *
 */
@Beta
@NonNullByDefault
public interface Pipeline<T> extends Registration {

    interface Builder {
        // FIXME: require invariants:

        //        - mandatory validation
        //        - unique validation
        //        - full normalization of anydata (and anyxml?)
        //        - output in RPC input order (automatically based on schema?)
        //        - 'must' constraints (against a data store instance?)
        //        - 'when' constraints (against a data store instance?)

        <T> Pipeline<T> bindOutput(Output<T> output) throws PipelineException;
    }

    interface Output<T> {

        // FIXME: what exactly?
    }

    // FIXME: this does not look quite right:

    sealed interface Progress {
        // Marker interface
    }

    record Completed<T>(T result) {
        public Completed {
            requireNonNull(result);
        }
    }

    final class NeedInput implements Progress {
        public static final NeedInput INSTANCE = new NeedInput();

        private NeedInput() {
            // Hidden on purpose
        }
    }

    // FIXME: document that these need to be assembled into a DistinctNodeContainer<NodeIdentifier, DataContainerChild>
    // FIXME: Mount points?
    record Result(List<? extends DataContainerChild> children) implements Progress {
        public Result {
            children = List.copyOf(children);
            final var unique = children.stream().map(DataContainerChild::name).distinct().count();
            if (unique != children.size()) {
                throw new IllegalArgumentException("non-unique children");
            }
        }
    }

    Progress tryProgress() throws PipelineException;

    default Result progressToResult() throws PipelineException {
        return switch (tryProgress()) {
            case NeedInput progress -> throw new PipelineException("Unexpected need for input");
            case Result progress -> progress;
        };
    }
}
