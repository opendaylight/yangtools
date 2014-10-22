package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Optional;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Before;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.MustDefinitionImpl;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;

/**
 * Created by lukas on 10/14/14.
 */
public abstract class AbstractBuilderTest {

    protected final static String MODULE_NAMESPACE = "urn:opendaylight.rpc:def:test-model";
    protected final static String REVISION = "2014-10-06";
    protected final static String MODULE_NAME = "test-module";
    protected final static DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-mm-dd");
    protected final static String MODULE_PATH = "test/module/path/test-module@" + REVISION + ".yang";

    protected ModuleBuilder module;

    @Before
    public void setUp() throws Exception {
        module = new ModuleBuilder(MODULE_NAME, MODULE_PATH);
        final Date moduleRevision = SIMPLE_DATE_FORMAT.parse(REVISION);
        module.setRevision(moduleRevision);
        module.setNamespace(URI.create(MODULE_NAMESPACE));
        module.setPrefix("mod");
    }

    protected MustDefinition provideMustDefinition() {
        return MustDefinitionImpl.create("must-condition == value",
            Optional.fromNullable("desc"), Optional.fromNullable("reference"), Optional.fromNullable("error-apptag"),
            Optional.fromNullable("error-message"));
    }

    protected UnknownSchemaNodeBuilder provideUnknownNodeBuilder() {
        final QName unknownType = QName.create(module.getNamespace(), module.getRevision(), "unknown-type");
        final QName unknownNode = QName.create(module.getNamespace(), module.getRevision(), "unknown-ext-use");
        final SchemaPath unknownNodePath = SchemaPath.create(true, unknownNode);
        final UnknownSchemaNodeBuilder unknownNodeBuilder = new UnknownSchemaNodeBuilderImpl(module.getModuleName(),
            25, unknownNode, unknownNodePath);
        unknownNodeBuilder.setNodeType(unknownType);
        return unknownNodeBuilder;
    }

    protected UnknownSchemaNodeBuilder provideUnknownNodeBuilder(final QName innerPathSegment) {
        final QName unknownType = QName.create(module.getNamespace(), module.getRevision(), "unknown-type");
        final QName unknownNode = QName.create(module.getNamespace(), module.getRevision(), "unknown-ext-use");
        final SchemaPath unknownNodePath = SchemaPath.create(true, innerPathSegment, unknownNode);
        final UnknownSchemaNodeBuilder unknownNodeBuilder = new UnknownSchemaNodeBuilderImpl(module.getModuleName(),
            25, unknownNode, unknownNodePath);
        unknownNodeBuilder.setNodeType(unknownType);
        return unknownNodeBuilder;
    }

    protected ChoiceBuilder provideChoiceBuilder(String choiceLocalName) {
        final QName choiceName = QName.create(module.getNamespace(), module.getRevision(), choiceLocalName);
        final SchemaPath choicePath = SchemaPath.create(true, choiceName);
        final ChoiceBuilder choiceBuilder = new ChoiceBuilder(module.getModuleName(), 22, choiceName, choicePath);

        return choiceBuilder;
    }

    protected UsesNodeBuilder provideUsesNodeBuilder(final String usesGroupingName) {
        final QName targetQName = QName.create(module.getNamespace(), module.getRevision(), usesGroupingName);
        return new UsesNodeBuilderImpl(module.getModuleName(), 10, SchemaPath.create(true, targetQName));
    }

    protected ContainerSchemaNodeBuilder provideContainerBuilder(final String containerLocalName) {
        final QName containerQName = QName.create(module.getNamespace(), module.getRevision(), containerLocalName);
        SchemaPath containerPath = SchemaPath.create(true, containerQName);

        return new ContainerSchemaNodeBuilder(module.getModuleName(), 10,
            containerQName, containerPath);
    }
}
