package org.opendaylight.yangtools.yang.model.parser.api;

import java.util.Arrays;
import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

public interface YangParserContext {
    /**
     * Add a set of sources to required build targets.
     *
     * @param sources add a set of sources into this context's build target.
     * @return This context.
     */
    YangParserContext addRequiredSources(Collection<? extends SchemaSourceRepresentation> sources);

    /**
     * Add a set of sources to required build targets.
     *
     * @param sources add a set of sources into this context's build target.
     * @return This context.
     * @throws IllegalArgumentException if specified source representation is not supported
     * @throws NullPointerException if any of the sources is null
     */
    default YangParserContext addRequiredSources(final SchemaSourceRepresentation... sources) {
        return addRequiredSources(Arrays.asList(sources));
    }

    /**
     * Add a set of sources to auxiliary library. These sources will be used to satisfy dependencies coming from
     * required build targets.
     *
     * @param sources add a set of sources to auxiliary library.
     * @return This context.
     * @throws IllegalArgumentException if specified source representation is not supported
     * @throws NullPointerException if any of the sources is null
     */
    YangParserContext addLibrarySources(Collection<SchemaSourceRepresentation> sources);

    /**
     * Add a set of sources to auxiliary library. These sources will be used to satisfy dependencies coming from
     * required build targets.
     *
     * @param sources add a set of sources into this context's build target.
     * @return This context.
     * @throws IllegalArgumentException if specified source representation is not supported
     * @throws NullPointerException if any of the sources is null
     */
    default YangParserContext addLibrarySources(final SchemaSourceRepresentation... sources) {
        return addLibrarySources(Arrays.asList(sources));
    }

    SchemaContext assembleSchemaContext() throws YangSyntaxErrorException;
}
