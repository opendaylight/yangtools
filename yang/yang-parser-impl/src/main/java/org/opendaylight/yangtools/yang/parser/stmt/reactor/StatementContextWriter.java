package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase.ContextBuilder;

class StatementContextWriter implements StatementWriter {

    private final SourceSpecificContext ctx;
    private StatementContextBase<?, ?, ?> parent;
    private ContextBuilder<?, ?, ?> current;
    private ModelProcessingPhase phase;

    public StatementContextWriter(SourceSpecificContext ctx, ModelProcessingPhase phase) {
        super();
        this.ctx = ctx;
        this.phase = phase;
    }

    @Override
    public void startStatement(QName name, StatementSourceReference ref) throws SourceException {
        defferedCreate();
        current = ctx.createDeclaredChild(parent, name, ref);

    }

    @Override
    public void argumentValue(String value, StatementSourceReference ref) {
        Preconditions.checkState(current != null, "Could not two arguments for one statement.");
        current.setArgument(value, ref);
    }

    void defferedCreate() throws SourceException {
        if(current != null) {
            parent = current.build();
            current = null;
        }
    }

    @Override
    public void endStatement(StatementSourceReference ref) throws SourceException {
        defferedCreate();
        Preconditions.checkState(parent != null);
        parent.endDeclared(ref,phase);
        parent = parent.getParentContext();
    }

}
