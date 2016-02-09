package org.opendaylight.yangtools.yang.model.repo.util;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;

public class RefcountedRegistrationTest {

    @Test
    public void refcountDecTrue() {
        final SchemaSourceRegistration<?> reg = Mockito.mock(SchemaSourceRegistration.class);
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        Assert.assertTrue(ref.decRef());
        Mockito.verify(reg, Mockito.times(1)).close();
    }

    @Test
    public void refcountIncDecFalse() {
        final SchemaSourceRegistration<?> reg = Mockito.mock(SchemaSourceRegistration.class);
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        ref.incRef();
        Assert.assertFalse(ref.decRef());
        Mockito.verify(reg, Mockito.times(0)).close();
    }

    @Test
    public void refcountIncDecTrue() {
        final SchemaSourceRegistration<?> reg = Mockito.mock(SchemaSourceRegistration.class);
        final RefcountedRegistration ref = new RefcountedRegistration(reg);
        ref.incRef();
        Assert.assertFalse(ref.decRef());
        Assert.assertTrue(ref.decRef());
        Mockito.verify(reg, Mockito.times(1)).close();
    }
}
