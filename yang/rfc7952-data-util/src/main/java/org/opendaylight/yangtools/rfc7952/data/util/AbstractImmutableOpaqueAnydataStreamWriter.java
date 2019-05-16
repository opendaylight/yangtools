package org.opendaylight.yangtools.rfc7952.data.util;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.data.api.OpaqueAnydataStreamWriter;
import org.opendaylight.yangtools.rfc7952.data.util.ImmutableNormalizedMetadata.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.util.schema.stream.AbstractOpaqueAnydataStreamWriter;

@Beta
public abstract class AbstractImmutableOpaqueAnydataStreamWriter extends AbstractOpaqueAnydataStreamWriter
        implements OpaqueAnydataStreamWriter {
    private final Deque<Builder> builders = new ArrayDeque<>();

    protected AbstractImmutableOpaqueAnydataStreamWriter(final boolean accurateLists) {
        super(accurateLists);
    }

    @Override
    public final void metadata(final ImmutableMap<QName, Object> metadata) throws IOException {
        final Builder current = builders.peek();
        checkState(current != null, "Attempted to emit metadata when no metadata is open");
        current.withAnnotations(metadata);
    }

    @Override
    protected final void enter(final NodeIdentifier name) throws IOException {
        builders.push(ImmutableNormalizedMetadata.builder().withIdentifier(name));
    }

    @Override
    protected final void exit() throws IOException {
        final ImmutableNormalizedMetadata metadata = builders.pop().build();
        final Builder current = verifyNotNull(builders.peek());
        if (!metadata.getAnnotations().isEmpty() || !metadata.getChildren().isEmpty()) {
            current.withChild(metadata);
        }
    }

    @Override
    protected final void finishAnydata(final OpaqueData opaqueData) {
        final ImmutableNormalizedMetadata metadata = builders.pop().build();
        verify(builders.isEmpty());
        finishAnydata(opaqueData, metadata);
    }

    abstract void finishAnydata(@NonNull OpaqueData opaqueData, ImmutableNormalizedMetadata metadata);
}
