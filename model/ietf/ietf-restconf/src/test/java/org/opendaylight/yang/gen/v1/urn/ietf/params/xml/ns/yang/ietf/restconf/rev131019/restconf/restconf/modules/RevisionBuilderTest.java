package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.RevisionIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.restconf.rev131019.restconf.restconf.modules.Module.Revision;

public class RevisionBuilderTest {
    
    private RevisionBuilder builder;
    
    @Before
    public void setUp()
    {
        builder = new RevisionBuilder();
    }
    
    @Test
    public void testEmptyString()
    {
        Revision revision = builder.getDefaultInstance( "" );
        validate( revision, "", null );
    }
    
    @Test
    public void testValidDataString()
    {
        String dateString = "2014-04-23";
        Revision revision = builder.getDefaultInstance( dateString );
        validate( revision, null, new RevisionIdentifier( dateString ) );
    }
    
    @Test( expected=IllegalArgumentException.class )
    public void testNullString()
    {
        builder.getDefaultInstance( null );
    }
    
    @Test( expected=IllegalArgumentException.class )
    public void testBadFormatString()
    {
        builder.getDefaultInstance( "badFormat" );
        
    }
    
    private void validate( Revision revisionUnderTest, String expectedRevisionString, RevisionIdentifier expectedRevisionIdentifier )
    {
        assertNotNull( revisionUnderTest );
        assertEquals( expectedRevisionString, revisionUnderTest.getString() );
        assertEquals( expectedRevisionIdentifier, revisionUnderTest.getRevisionIdentifier() );
    }
}
