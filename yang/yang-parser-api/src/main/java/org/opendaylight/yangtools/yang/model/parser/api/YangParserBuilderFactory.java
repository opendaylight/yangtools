package org.opendaylight.yangtools.yang.model.parser.api;

import java.util.Set;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

/**
 * Service-like factory anchor. This is the primary entry point into the parser API, providing a source
 * of {@link YangParserBuilder}s.
 *
 * @author Robert Varga
 */
public interface YangParserBuilderFactory {
    /**
     * Return the set of {@link SchemaSourceRepresentation}s supported by this factory.
     *
     * @return Set of supported representations.
     */
    Set<? extends SchemaSourceRepresentation> supportedRepresentations();

    /**
     * Create a new YangParserBuilder.
     *
     * @return A fresh builder.
     */
    YangParserBuilder newBuilder();
}
