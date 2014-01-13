package org.opendaylight.yangtools.yang.parser.impl.util;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class YangSourceContext implements SchemaSourceProvider<InputStream>,AutoCloseable {

    private final ImmutableSet<SourceIdentifier> validSources;


    private final ImmutableSet<SourceIdentifier> missingSources;
    private final ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependencies;
    private SchemaSourceProvider<InputStream> sourceProvider;

    private YangSourceContext(ImmutableSet<SourceIdentifier> validSourcesSet,
            ImmutableSet<SourceIdentifier> missingSourcesSet,
            ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependenciesMap, SchemaSourceProvider<InputStream> sourceProvicer) {
        validSources = validSourcesSet;
        missingSources = missingSourcesSet;
        missingDependencies = missingDependenciesMap;
        sourceProvider = sourceProvicer;
    }

    public ImmutableSet<SourceIdentifier> getValidSources() {
        return validSources;
    }

    public ImmutableSet<SourceIdentifier> getMissingSources() {
        return missingSources;
    }

    public ImmutableMultimap<SourceIdentifier, ModuleImport> getMissingDependencies() {
        return missingDependencies;
    }
    
    @Override
    public Optional<InputStream> getSchemaSource(String moduleName, Optional<String> revision) {
        return getSchemaSource(SourceIdentifier.create(moduleName,revision));
    }
    
    @Override
    public Optional<InputStream> getSchemaSource(SourceIdentifier sourceIdentifier) {
        if(validSources.contains(sourceIdentifier)) {
            return getDelegateChecked().getSchemaSource(sourceIdentifier);
        }
        return Optional.absent();
    }
    
    private SchemaSourceProvider<InputStream> getDelegateChecked() {
        Preconditions.checkState(sourceProvider != null,"Instance is already closed.");
        return sourceProvider;
    }

    @Override
    public void close() {
        if(sourceProvider != null) {
            sourceProvider = null;
        }
    }
    
    public static final YangSourceContext createFrom(Iterable<QName> capabilities,
            SchemaSourceProvider<InputStream> schemaSourceProvider) {
        YangSourceContextResolver resolver = new YangSourceFromCapabilitiesResolver(capabilities, schemaSourceProvider);
        return resolver.resolveContext();
    }
    
    public static final SchemaContext toSchemaContext(YangSourceContext context) {
        List<InputStream> inputStreams =  getValidInputStreams(context);
        YangParserImpl parser = new YangParserImpl();
        Set<Module> models = parser.parseYangModelsFromStreams(inputStreams);
        return parser.resolveSchemaContext(models);
    }
    
    public static List<InputStream> getValidInputStreams(YangSourceContext context) {
        return getValidInputStreams(context, context.sourceProvider);
    }
    
    public static List<InputStream> getValidInputStreams(YangSourceContext context, SchemaSourceProvider<InputStream> provider) {
        // TODO Auto-generated method stub
        final HashSet<SourceIdentifier> sourcesToLoad = new HashSet<>();
        sourcesToLoad.addAll(context.getValidSources());
        for(SourceIdentifier source : context.getValidSources()) {
            if(source.getRevision() != null) {
                SourceIdentifier sourceWithoutRevision = SourceIdentifier.create(source.getName(), Optional.<String>absent());
                sourcesToLoad.removeAll(Collections.singleton(sourceWithoutRevision));
            }
        }
        
        ImmutableList.Builder<InputStream> ret = ImmutableList.<InputStream>builder();
        for(SourceIdentifier sourceIdentifier : sourcesToLoad) {
            Optional<InputStream> source = provider.getSchemaSource(sourceIdentifier);
            ret.add(source.get());
        }
        return ret.build();
    }


    public static abstract class YangSourceContextResolver {
        
        private static final Logger LOG = LoggerFactory.getLogger(YangSourceContextResolver.class);

        private SchemaSourceProvider<InputStream> sourceProvicer;

        private HashMap<SourceIdentifier, ResolutionState> alreadyProcessed = new HashMap<>();

        private ImmutableSet.Builder<SourceIdentifier> missingSources = ImmutableSet.builder();

        private ImmutableMultimap.Builder<SourceIdentifier, ModuleImport> missingDependencies = ImmutableMultimap
                .builder();

        private ImmutableSet.Builder<SourceIdentifier> validSources = ImmutableSet.builder();

        public YangSourceContextResolver(SchemaSourceProvider<InputStream> schemaSourceProvider) {
            sourceProvicer = schemaSourceProvider;
        }

        public abstract YangSourceContext resolveContext();

        public ResolutionState resolveSource(String name, Optional<String> formattedRevision) {
            return resolveSource(new SourceIdentifier(name, formattedRevision));
        }

        private ResolutionState resolveSource(SourceIdentifier identifier) {
            
            if (alreadyProcessed.containsKey(identifier)) {
                return alreadyProcessed.get(identifier);
            }
            LOG.info("Resolving source: {}",identifier);
            ResolutionState potentialState = ResolutionState.EVERYTHING_OK;
            try {
                Optional<InputStream> source = getSchemaSource(identifier);
                if (source.isPresent()) {

                    YangModelDependencyInfo info = YangModelDependencyInfo.fromInputStream(source.get());

                    checkValidSource(identifier,info);

                    for (ModuleImport dependency : info.getDependencies()) {
                        LOG.debug("Source: {} Resolving dependency: {}",identifier,dependency);
                        ResolutionState dependencyState = resolveDependency(dependency);
                        if (dependencyState == ResolutionState.MISSING_SOURCE) {
                            potentialState = ResolutionState.MISSING_DEPENDENCY;
                            missingDependencies.put(identifier, dependency);
                        }
                    }
                } else {
                    missingSources.add(identifier);
                    return ResolutionState.MISSING_SOURCE;
                } 
            } catch (Exception e) {
                potentialState = ResolutionState.OTHER_ERROR;
            }
            updateResolutionState(identifier, potentialState);
            return potentialState;
        }

        private boolean checkValidSource(SourceIdentifier identifier, YangModelDependencyInfo info) {
            if(!identifier.getName().equals(info.getName())) {
                LOG.warn("Incorrect model returned. Identifier name was: {}, source contained: {}", identifier.getName(),info.getName());
                throw new IllegalStateException("Incorrect source was returned");
            }
            return true;
        }

        private void updateResolutionState(SourceIdentifier identifier, ResolutionState potentialState) {
            alreadyProcessed.put(identifier, potentialState);
            switch (potentialState) {
            case MISSING_SOURCE:
                missingSources.add(identifier);
                break;
            case EVERYTHING_OK:
                validSources.add(identifier);
                break;
           default:
                break;
            }
        }

        private ResolutionState resolveDependency(ModuleImport dependency) {
            String name = dependency.getModuleName();
            Optional<String> formattedRevision = Optional
                    .fromNullable(QName.formattedRevision(dependency.getRevision()));
            return resolveSource(new SourceIdentifier(name, formattedRevision));
        }

        private Optional<InputStream> getSchemaSource(SourceIdentifier identifier) {
            return sourceProvicer
                    .getSchemaSource(identifier.getName(), Optional.fromNullable(identifier.getRevision()));
        }

        protected YangSourceContext createSourceContext() {
            
            ImmutableSet<SourceIdentifier> missingSourcesSet = missingSources.build();
            ImmutableMultimap<SourceIdentifier, ModuleImport> missingDependenciesMap = missingDependencies.build();
            ImmutableSet<SourceIdentifier> validSourcesSet = validSources.build();
            
            
            return new YangSourceContext(validSourcesSet,missingSourcesSet,missingDependenciesMap,sourceProvicer);
            
        }
    }

    private enum ResolutionState {
        MISSING_SOURCE, MISSING_DEPENDENCY, OTHER_ERROR, EVERYTHING_OK
    }

    public static final class YangSourceFromCapabilitiesResolver extends YangSourceContextResolver {

        private Iterable<QName> capabilities;

        public YangSourceFromCapabilitiesResolver(Iterable<QName> capabilities,
                SchemaSourceProvider<InputStream> schemaSourceProvider) {
            super(schemaSourceProvider);
            this.capabilities = capabilities;
        }

        @Override
        public YangSourceContext resolveContext() {
            for (QName capability : capabilities) {
                resolveCapability(capability);
            }
            return createSourceContext();
        }

        private void resolveCapability(QName capability) {
            super.resolveSource(capability.getLocalName(), Optional.fromNullable(capability.getFormattedRevision()));
        }
    }


}
