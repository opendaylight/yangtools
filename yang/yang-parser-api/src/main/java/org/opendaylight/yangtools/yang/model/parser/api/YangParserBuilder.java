package org.opendaylight.yangtools.yang.model.parser.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

public abstract class YangParserBuilder implements Builder<YangParser> {
    private final Set<Class<? extends SchemaSourceRepresentation>> requiredRepresentationSupport = new HashSet<>();
    private ImportResolutionPolicy importResolutionPolicy = ImportResolutionPolicy.REVISION;

    @Nonnull
    protected final ImportResolutionPolicy getImportResolutionPolicy() {
        return importResolutionPolicy;
    }

    /**
     *
     * @param policy new import resolution policy
     * @return This builder.
     */
    public YangParserBuilder setImportResolutionPolicy(@Nonnull final ImportResolutionPolicy policy) {
        checkArgument(isImportResolutionPolicySupported(requireNonNull(policy)),
            "Unsupported import resolution policy %s", policy);
        this.importResolutionPolicy = requireNonNull(policy);
        return this;
    }

    /**
     *
     * @param representation required representation type
     * @return This builder.
     * @throws NullPointerException if representation is null
     * @throws IllegalArgumentException if representation is not supported
     */
    public YangParserBuilder requireRepresentationSupport(
            @Nonnull final Class<? extends SchemaSourceRepresentation> representation) {
        checkArgument(isRepresentationSupported(requireNonNull(representation)), "Unsupported representation %s",
            representation);
        requiredRepresentationSupport.add(representation);
        return this;
    }

    protected abstract boolean isImportResolutionPolicySupported(@Nonnull ImportResolutionPolicy policy);

    protected abstract boolean isRepresentationSupported(
            @Nonnull Class<? extends SchemaSourceRepresentation> representation);

    /**
     * Build a new {@link YangParser}.
     *
     * @return A new {@link YangParser}.
     * @throws IllegalStateException if the parser is not fully defined.
     */
    @Override
    public abstract YangParser build();
}
