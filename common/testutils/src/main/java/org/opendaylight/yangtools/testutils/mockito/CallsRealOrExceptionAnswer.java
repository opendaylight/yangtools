/*
 * Copyright (c) 2016 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.testutils.mockito;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito Answer which for un-stubbed methods forwards the call to the real
 * method if it is implemented on the mocked object (i.e. not an interface or
 * abstract method), and otherwise throws an {@link UnstubbedMethodException}, like the
 * {@link ThrowsMethodExceptionAnswer}.
 *
 * <p>
 * This can be useful to create light-weight <a href=
 * "http://googletesting.blogspot.ch/2013/07/testing-on-toilet-know-your-test-doubles.html">Fake Doubles</a>
 * (in particular some with state). For example:
 *
 * <pre>
 * import static ...testutils.mockito.MoreAnswers.realOrException;
 *
 * interface Service {
 *     List&lt;Thing&gt; getThings();
 *     boolean installThing(Thing thing);
 * }
 *
 * abstract class FakeService implements Service {
 *     // Ignore getThings() - we don't need that for this test
 *     boolean installThing(Thing thing) {
 *         LOGGER.log("not really installed");
 *         return false;
 *     }
 * }
 *
 * Service fake = Mockito.mock(FakeService.class, realOrException())
 * </pre>
 *
 * <p>
 * TIP: An impact of Mockito is that, just like in standard Mockito, constructors
 * (and thus field initializers) are not called. So in your abstract fake class,
 * instead of:
 *
 * <pre>
 * abstract class FakeService implements Service {
 *     private final List&lt;Thing&gt; things = new ArrayList&lt;&gt;();
 *
 *     public List&lt;Thing&gt; getThings() {
 *         return things;
 *     }
 *
 *     &#64;Override
 *     public boolean installThing(Thing thing) {
 *         return things.add(thing);
 *     }
 * }
 * </pre>
 *
 * <p>
 * you'll just need to do:
 *
 * <pre>
 * abstract class FakeService implements Service {
 *     private List&lt;Thing&gt; things;
 *
 *     public List&lt;Thing&gt; getThings() {
 *         if (things == null)
 *             things = new ArrayList&lt;&gt;()
 *         return things;
 *     }
 *
 *     &#64;Override
 *     public boolean installThing(Thing thing) {
 *         return getThings().add(thing);
 *     }
 * }
 * </pre>
 *
 * <p>
 * The big advantage of Mikitos versus just writing classes implementing service
 * interfaces without using Mockito at all is that you don't have to implement a
 * lot of methods you don't care about - you can just make an abstract fake
 * class (incl. e.g. an inner class in your Test) and implement only one or some
 * methods. This keeps code shorter and thus more readable.
 *
 * <p>
 * The advantage of Mikitos VS pure Mockito's when/thenAnswer are that they:
 * <ul>
 *
 * <li>are fully type safe and refactoring resistant; whereas Mockito is not,
 * e.g. for return values with doReturn(...).when(), and uses runtime instead of
 * compile time error reporting for this.</li>
 * <li>avoid confusion re. the alternative doReturn(...).when() syntax required
 * with ThrowsMethodExceptionAnswer instead of when(...).thenReturn()</li>
 * <li>enforce the ThrowsMethodExceptionAnswer by default for
 * non-implemented methods (which is possible with Mockito by explicitly passing
 * this, but is easily forgotten)</li>
 * </ul>
 *
 * @see Mockito#mock(Class, Answer)
 * @see ThrowsMethodExceptionAnswer
 * @see Mockito#CALLS_REAL_METHODS
 * @see Mockito#CALLS_REAL_METHODS
 *
 * @author Michael Vorburger
 */
@Beta
public class CallsRealOrExceptionAnswer implements Answer<Object>, Serializable {
    private static final long serialVersionUID = -3730024662402964588L;
    static final CallsRealOrExceptionAnswer INSTANCE = new CallsRealOrExceptionAnswer();

    private CallsRealOrExceptionAnswer() {

    }

    @Override
    public Object answer(final InvocationOnMock invocation) throws Throwable {
        if (Modifier.isAbstract(invocation.getMethod().getModifiers())) {
            throw new UnstubbedMethodException(invocation.getMethod(), invocation.getMock());
        }
        return invocation.callRealMethod();
    }

    Object readResolve() {
        return INSTANCE;
    }
}
