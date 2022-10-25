/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamPushTask;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * @author nite
 *
 */
@Beta
@NonNullByDefault
public final class JSONNormalizedNodeStreamPushTask extends NormalizedNodeStreamPushTask {

    private JSONNormalizedNodeStreamPushTask() {
        // Hidden on purpose
    }

    @Override
    protected List<PathArgument> executeImpl() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public static StrictBuilderStage builder() {
        return new StrictBuilderStage();
    }

    public static final class StrictBuilderStage extends RequireCodecFactoryBuilderStage {
        private StrictBuilderStage() {
            super(false);
        }

        @SuppressWarnings("static-method")
        public RequireCodecFactoryBuilderStage lenient() {
            return new RequireCodecFactoryBuilderStage(true);
        }
    }

    public static sealed class RequireCodecFactoryBuilderStage {
        private final boolean lenient;

        private RequireCodecFactoryBuilderStage(final boolean lenient) {
            this.lenient = lenient;
        }

        public OptionalInferenceBuilderStage withCodecFactory(final JSONCodecFactory codecFactory) {
            return new OptionalInferenceBuilderStage(lenient, codecFactory);
        }
    }

    public static final class OptionalInferenceBuilderStage extends JSONRequireWriterBuilderStage {
        private OptionalInferenceBuilderStage(final boolean lenient, final JSONCodecFactory codecFactory) {
            super(lenient, codecFactory);
        }

        // FIXME: does this point to the document, document parent, or what?
        // FIXME: also: do we fancy a YangInstanceIdentifier-based instantiation?
        // FIXME: also: invocation parsing?
        // FIXME: finally: how does this work with mount points?
        public RequireWriterBuilderStage withInference(final EffectiveStatementInference inference) {
            return new JSONRequireWriterBuilderStage(lenient, codecFactory, inference);
        }
    }

    private static sealed class JSONRequireWriterBuilderStage implements RequireWriterBuilderStage {
        private final @Nullable EffectiveStatementInference inference;
        final boolean lenient;
        final JSONCodecFactory codecFactory;

        JSONRequireWriterBuilderStage(final boolean lenient, final JSONCodecFactory codecFactory) {
            this.lenient = lenient;
            this.codecFactory = requireNonNull(codecFactory);
            inference = null;
        }

        JSONRequireWriterBuilderStage(final boolean lenient, final JSONCodecFactory codecFactory,
                final EffectiveStatementInference inference) {
            this.lenient = lenient;
            this.codecFactory = requireNonNull(codecFactory);
            this.inference = requireNonNull(inference);
        }

        @Override
        public JSONFinalBuilderStage withWriter(NormalizedNodeStreamWriter writer) {
            return new JSONFinalBuilderStage(lenient, codecFactory, inference);
        }
    }

    private static final class JSONFinalBuilderStage implements FinalBuilderStage {
        private final @Nullable EffectiveStatementInference inference;
        private final JSONCodecFactory codecFactory;
        private final boolean lenient;

        JSONFinalBuilderStage(final boolean lenient, final JSONCodecFactory codecFactory,
                final @Nullable EffectiveStatementInference inference) {
            this.lenient = lenient;
            this.codecFactory = requireNonNull(codecFactory);
            this.inference = inference;
        }

        @Override
        public JSONNormalizedNodeStreamPushTask build() {
            return new JSONNormalizedNodeStreamPushTask();
        }
    }
}
