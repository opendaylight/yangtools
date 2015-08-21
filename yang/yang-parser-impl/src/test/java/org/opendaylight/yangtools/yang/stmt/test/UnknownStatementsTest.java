package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertNotNull;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.junit.Test;

public class UnknownStatementsTest {

    @Test
    public void unknownStatementsTest() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/semantic-statement-parser/unknown-statements");
        assertNotNull(context);
    }

}
