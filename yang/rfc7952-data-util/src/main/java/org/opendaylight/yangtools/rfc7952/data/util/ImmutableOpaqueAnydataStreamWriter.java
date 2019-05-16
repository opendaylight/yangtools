package org.opendaylight.yangtools.rfc7952.data.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.opendaylight.yangtools.rfc7952.data.api.OpaqueAnydataStreamWriter;
import org.opendaylight.yangtools.rfc7952.data.util.ImmutableNormalizedMetadata.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.util.schema.stream.AbstractOpaqueAnydataStreamWriter;

@Beta
public abstract class ImmutableOpaqueAnydataStreamWriter extends AbstractOpaqueAnydataStreamWriter
        implements OpaqueAnydataStreamWriter {
    private final Deque<Builder> builders = new ArrayDeque<>();

    protected ImmutableOpaqueAnydataStreamWriter(final boolean accurateLists) {
        super(accurateLists);
    }

    @Override
    protected void enter(final NodeIdentifier name) throws IOException {
        // FIXME: implement this
    }

    @Override
    protected void exit() throws IOException {
        // FIXME: implement this
    }

    @Override
    public void metadata(final ImmutableMap<QName, Object> metadata) throws IOException {
        // TODO Auto-generated method stub
    }
}
