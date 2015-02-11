package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupportBundle;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;

public class CrossSourceStatementReactor {

    final Map<ModelProcessingPhase,StatementSupportBundle> supportedTerminology;

    CrossSourceStatementReactor(Map<ModelProcessingPhase, StatementSupportBundle> supportedTerminology) {
        this.supportedTerminology = ImmutableMap.copyOf(supportedTerminology);
    }

    public static final Builder builder() {
        return new Builder();
    }

    public final BuildAction newBuild() {
        return new BuildAction();
    }

    public static class Builder implements org.opendaylight.yangtools.concepts.Builder<CrossSourceStatementReactor>{

        final Map<ModelProcessingPhase,StatementSupportBundle> bundles = new EnumMap<>(ModelProcessingPhase.class);

        public Builder setBundle(ModelProcessingPhase phase,StatementSupportBundle bundle) {
            bundles.put(phase, bundle);
            return this;
        }

        @Override
        public CrossSourceStatementReactor build() {
            return new CrossSourceStatementReactor(bundles);
        }

    }

    public class BuildAction {

        private final BuildGlobalContext context;

        public BuildAction() {
            this.context = new BuildGlobalContext(supportedTerminology);
        }

        public void addSource(StatementStreamSource source) {
            context.addSource(source);
        }

        public EffectiveModelContext build() throws SourceException, ReactorException {
            return context.build();
        }



    }


}
