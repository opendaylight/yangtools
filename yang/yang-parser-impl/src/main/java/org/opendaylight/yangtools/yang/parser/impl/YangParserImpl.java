package org.opendaylight.yangtools.yang.parser.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinDomSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinXmlSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YinStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.rfc6020.repo.YinTextToDomTransformer;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.xml.sax.SAXException;

final class YangParserImpl implements YangParser {
    private final BuildAction buildAction;

    YangParserImpl(final BuildAction buildAction) {
        this.buildAction = requireNonNull(buildAction);
    }

    @Override
    public YangParser addSource(final SchemaSourceRepresentation source) throws IOException, YangSyntaxErrorException {
        buildAction.addSource(sourceToStatementStream(source));
        return this;
    }

    @Override
    public YangParser addLibSource(final SchemaSourceRepresentation source)
            throws IOException, YangSyntaxErrorException {
        buildAction.addLibSources(sourceToStatementStream(source));
        return this;
    }

    @Override
    public YangParser setSupportedFeatures(final Set<QName> supportedFeatures) {
        buildAction.setSupportedFeatures(supportedFeatures);
        return this;
    }

    @Override
    public YangParser setModulesWithSupportedDeviations(
            final Map<QNameModule, Set<QNameModule>> modulesDeviatedByModules) {
        buildAction.setModulesWithSupportedDeviations(modulesDeviatedByModules);
        return this;
    }

    @Override
    public List<DeclaredStatement<?>> buildDeclaredModel() throws YangSyntaxErrorException {
        try {
            return buildAction.build().getRootStatements();
        } catch (ReactorException e) {
            // FIXME: map exception in some reasonable manner
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SchemaContext buildSchemaContext() throws YangSyntaxErrorException {
        try {
            return buildAction.buildEffective();
        } catch (ReactorException e) {
            throw decodeReactorException(e);
        }
    }

    private static RuntimeException decodeReactorException(final ReactorException reported)
            throws YangSyntaxErrorException {
        // FIXME: map exception in some reasonable manner
         return null;
    }

    private static StatementStreamSource sourceToStatementStream(final SchemaSourceRepresentation source)
            throws IOException, YangSyntaxErrorException {
        requireNonNull(source);
        if (source instanceof YangTextSchemaSource) {
            return YangStatementStreamSource.create((YangTextSchemaSource) source);
        } else if (source instanceof YinDomSchemaSource) {
            return YinStatementStreamSource.create((YinDomSchemaSource) source);
        } else if (source instanceof YinTextSchemaSource) {
            try {
                return YinStatementStreamSource.create(YinTextToDomTransformer.transformSource(
                    (YinTextSchemaSource) source));
            } catch (SAXException e) {
                throw new YangSyntaxErrorException(source.getIdentifier(), 0, 0, "Failed to parse XML text", e);
            }
        } else if (source instanceof YinXmlSchemaSource) {
            try {
                return YinStatementStreamSource.create((YinXmlSchemaSource) source);
            } catch (TransformerException e) {
                throw new YangSyntaxErrorException(source.getIdentifier(), 0, 0,
                    "Failed to assemble in-memory representation", e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported source " + source);
        }
    }
}
