package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

import org.opendaylight.yangtools.yang.parser.base.meta.CrossSourceReactor;

public class YangInferencePipeline {


    public static final CrossSourceReactor createDefaultReactor() {
        CrossSourceReactor reactor = new CrossSourceReactor();
        reactor.addSupport(new ModuleStatementImpl.Definition(),ModelProcessingPhase.Linkage);
        reactor.addSupport(new NamespaceStatementImpl.Definition(),ModelProcessingPhase.Linkage);
        reactor.addSupport(new ImportStatementImpl.Definition(),ModelProcessingPhase.Linkage);
        reactor.addSupport(new PrefixStatementImpl.Definition(),ModelProcessingPhase.Linkage);
        reactor.addSupport(YangNamespaceSupport.MODULE,ModelProcessingPhase.Linkage);
        return reactor;
    }

}
