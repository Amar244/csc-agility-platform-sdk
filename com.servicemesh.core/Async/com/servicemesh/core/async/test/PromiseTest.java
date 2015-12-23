/**
 *              COPYRIGHT (C) 2008-2015 SERVICEMESH, INC.
 *                        ALL RIGHTS RESERVED.
 *                   CONFIDENTIAL AND PROPRIETARY.
 *
 *  ALL SOFTWARE, INFORMATION AND ANY OTHER RELATED COMMUNICATIONS
 *  (COLLECTIVELY, "WORKS") ARE CONFIDENTIAL AND PROPRIETARY INFORMATION THAT
 *  ARE THE EXCLUSIVE PROPERTY OF SERVICEMESH.
 *  ALL WORKS ARE PROVIDED UNDER THE APPLICABLE AGREEMENT OR END USER LICENSE
 *  AGREEMENT IN EFFECT BETWEEN YOU AND SERVICEMESH.  UNLESS OTHERWISE SPECIFIED
 *  IN THE APPLICABLE AGREEMENT, ALL WORKS ARE PROVIDED "AS IS" WITHOUT WARRANTY
 *  OF ANY KIND EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
 *  ALL USE, DISCLOSURE AND/OR REPRODUCTION OF WORKS NOT EXPRESSLY AUTHORIZED BY
 *  SERVICEMESH IS STRICTLY PROHIBITED.
 */

package com.servicemesh.core.async.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

import com.servicemesh.core.async.Callback;
import com.servicemesh.core.async.CompletablePromise;
import com.servicemesh.core.async.Function;
import com.servicemesh.core.async.Promise;
import com.servicemesh.core.async.PromiseFactory;
import com.servicemesh.core.reactor.Reactor;
import com.servicemesh.core.reactor.WorkHandler;
import com.servicemesh.core.reactor.WorkReactor;

public class PromiseTest
{
    @Test
    public void testComplete()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        Assert.assertFalse(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.complete(100);
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();
        Assert.assertEquals(Integer.valueOf(100), promise.get());
        Assert.assertEquals(Integer.valueOf(100), promise.get(100, TimeUnit.MILLISECONDS));
        Assert.assertTrue(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());

        // Test second complete
        try {
            promise.complete(-1);
            Assert.fail("Did not throw expected IllegalStateException after second complete");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }
        Assert.assertTrue(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());
        Assert.assertEquals(Integer.valueOf(100), promise.get());
        Assert.assertEquals(Integer.valueOf(100), promise.get(100, TimeUnit.MILLISECONDS));

        // Test setting failure after completion
        try {
            promise.failure(new Exception("Test"));
            Assert.fail("Did not throw expected IllegalStateException for failure() after complete()");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }
        Assert.assertTrue(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());
        Assert.assertEquals(Integer.valueOf(100), promise.get());
        Assert.assertEquals(Integer.valueOf(100), promise.get(100, TimeUnit.MILLISECONDS));

        // Test cancel after completion
        try {
            promise.cancel();
            Assert.fail("Did not throw expected IllegalStateException for cancel() after complete()");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }
        Assert.assertTrue(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());
        Assert.assertEquals(Integer.valueOf(100), promise.get());
        Assert.assertEquals(Integer.valueOf(100), promise.get(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testFailure()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        Assert.assertFalse(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.failure(new IllegalArgumentException("Test"));
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();

        try {
            promise.get();
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertTrue(promise.isFailed());

        // Test second failure
        try {
            promise.failure(new IndexOutOfBoundsException("Test part 2"));
            Assert.fail("Did not throw expected IllegalStateException after second failure");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }

        try {
            promise.get();
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertTrue(promise.isFailed());

        // Test completion after failure
        try {
            promise.complete(100);
            Assert.fail("Did not throw expected IllegalStateException on complete after failure");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }

        try {
            promise.get();
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertTrue(promise.isFailed());

        // Test cancel after failure
        try {
            promise.cancel();
            Assert.fail("Did not throw expected IllegalStateException on cancel() after failure");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }

        try {
            promise.get();
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Succeeded when exception set");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Test".equals(ex.getMessage()));
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertTrue(promise.isFailed());
    }

    @Test
    public void testCancel()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        Assert.assertFalse(promise.isCompleted());
        Assert.assertFalse(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.cancel();
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();

        try {
            promise.get();
            Assert.fail("Succeeded when promise cancelled");
        } catch (CancellationException ex) {
            Assert.assertTrue("Promise was cancelled".equals(ex.getMessage()));
        } catch (Throwable th) {
            Assert.fail("Wrong exception thrown");
        }

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Succeeded when promise cancelled");
        } catch (CancellationException ex) {
            Assert.assertTrue("Promise was cancelled".equals(ex.getMessage()));
        } catch (Throwable th) {
            Assert.fail("Wrong exception thrown");
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertTrue(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());

        // Test second cancel
        try {
            promise.cancel();
            Assert.fail("Did not throw expected IllegalStateException after second cancel");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }

        try {
            promise.get();
            Assert.fail("Succeeded when exception set");
        } catch (CancellationException ex) {
            Assert.assertTrue("Promise was cancelled".equals(ex.getMessage()));
        } catch (Throwable th) {
            Assert.fail("Wrong exception thrown");
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertTrue(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());

        // Test completion after cancel
        try {
            promise.complete(100);
            Assert.fail("Did not throw expected IllegalStateException on complete after cancel");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }

        try {
            promise.get();
            Assert.fail("Succeeded when promise cancelled");
        } catch (CancellationException ex) {
            Assert.assertTrue("Promise was cancelled".equals(ex.getMessage()));
        } catch (Throwable th) {
            Assert.fail("Wrong exception thrown");
        }

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Succeeded when promise cancelled");
        } catch (CancellationException ex) {
            Assert.assertTrue("Promise was cancelled".equals(ex.getMessage()));
        } catch (Throwable th) {
            Assert.fail("Wrong exception thrown");
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertTrue(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());

        // Test failure after cancel
        try {
            promise.failure(new NullPointerException("Test"));
            Assert.fail("Did not throw expected IllegalStateException on complete after cancel");
        } catch (IllegalStateException ex) {
            // Ignore, expected
        }

        try {
            promise.get();
            Assert.fail("Succeeded when promise cancelled");
        } catch (CancellationException ex) {
            Assert.assertTrue("Promise was cancelled".equals(ex.getMessage()));
        } catch (Throwable th) {
            Assert.fail("Wrong exception thrown");
        }

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Succeeded when promise cancelled");
        } catch (CancellationException ex) {
            Assert.assertTrue("Promise was cancelled".equals(ex.getMessage()));
        } catch (Throwable th) {
            Assert.fail("Wrong exception thrown");
        }

        Assert.assertFalse(promise.isCompleted());
        Assert.assertTrue(promise.isCancelled());
        Assert.assertFalse(promise.isFailed());
    }

    @Test
    public void testGetTimeout()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();

        try {
            promise.get(100, TimeUnit.MILLISECONDS);
            Assert.fail("Expected timeout exception");
        } catch (TimeoutException ex) {
            // Ignore, expected
        }

        promise.complete(Integer.valueOf(100));
        Assert.assertEquals(Integer.valueOf(100), promise.get(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testGetReactor()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final Reactor reactor = WorkReactor.getDefaultWorkReactor();
        final List<Integer> values = new ArrayList<Integer>();
        final List<Throwable> failures = new ArrayList<Throwable>();

        reactor.workCreate(new WorkHandler() {
            @Override
            public boolean workFire() {
                try {
                    values.add(promise.get(reactor));
                } catch (Throwable th) {
                    failures.add(th);
                }

                return false;
            }
        });

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.complete(111);
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();

        promise.get(1, TimeUnit.SECONDS);  // Give the reactor time to fire
        Thread.sleep(200);

        if (failures.size() > 0) {
            throw failures.get(0);
        }

        Assert.assertEquals(1, values.size());
        Assert.assertEquals(Integer.valueOf(111), values.get(0));
    }

    @Test
    public void testGetReactorFailure()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final Reactor reactor = WorkReactor.getDefaultWorkReactor();
        final List<Integer> values = new ArrayList<Integer>();
        final List<Throwable> failures = new ArrayList<Throwable>();

        reactor.workCreate(new WorkHandler() {
            @Override
            public boolean workFire() {
                try {
                    values.add(promise.get(reactor));
                } catch (Throwable th) {
                    failures.add(th);
                }

                return false;
            }
        });

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.failure(new IllegalArgumentException("Test"));
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();

        // Give the reactor time to fire
        try {
            promise.get(1, TimeUnit.SECONDS);
        } catch (Throwable th) {
            // Ignore, expected
        }
        Thread.sleep(200);

        Assert.assertEquals(1, failures.size());
        Assert.assertTrue(failures.get(0) instanceof IllegalArgumentException);
    }

    @Test
    public void testGetReactorCancel()
        throws Throwable
    {
        final Promise<Integer> promise = PromiseFactory.create();
        final Reactor reactor = WorkReactor.getDefaultWorkReactor();
        final List<Integer> values = new ArrayList<Integer>();
        final List<Throwable> failures = new ArrayList<Throwable>();

        reactor.workCreate(new WorkHandler() {
            @Override
            public boolean workFire() {
                try {
                    values.add(promise.get(reactor));
                } catch (Throwable th) {
                    failures.add(th);
                }

                return false;
            }
        });

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.cancel();
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();

        // Give the reactor time to fire
        try {
            promise.get(1, TimeUnit.SECONDS);
        } catch (Throwable th) {
            // Ignore, expected
        }
        Thread.sleep(200);

        Assert.assertEquals(1, failures.size());
        Assert.assertTrue(failures.get(0) instanceof CancellationException);
    }

    @Test
    public void testCompletionCallback()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final CompletionCallback completionCallback1 = new CompletionCallback();
        final CompletionCallback completionCallback2 = new CompletionCallback();
        final FailureCallback failureCallback = new FailureCallback();
        final CancelCallback cancelCallback = new CancelCallback();

        promise.onComplete(completionCallback1);
        promise.onComplete(completionCallback2);
        promise.onFailure(failureCallback);
        promise.onCancel(cancelCallback);
        Assert.assertNull(completionCallback1.getValue());
        Assert.assertNull(completionCallback2.getValue());
        Assert.assertNull(failureCallback.getValue());
        Assert.assertFalse(cancelCallback.isCancelled());

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.complete(101);
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();
        Assert.assertEquals(Integer.valueOf(101), promise.get(1, TimeUnit.SECONDS));
        Thread.sleep(200);      // Give the promise time to complete the callbacks
        Assert.assertEquals(101, completionCallback1.getValue().intValue());
        Assert.assertEquals(101, completionCallback2.getValue().intValue());
        Assert.assertNull(failureCallback.getValue());
        Assert.assertFalse(cancelCallback.isCancelled());

        // Add another callback after completion
        final CompletionCallback completionCallback3 = new CompletionCallback();
        promise.onComplete(completionCallback3);
        Assert.assertEquals(101, completionCallback3.getValue().intValue());
    }

    @Test
    public void testFailureCallback()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final FailureCallback failureCallback1 = new FailureCallback();
        final FailureCallback failureCallback2 = new FailureCallback();
        final CompletionCallback completionCallback = new CompletionCallback();
        final CancelCallback cancelCallback = new CancelCallback();

        promise.onFailure(failureCallback1);
        promise.onFailure(failureCallback2);
        promise.onComplete(completionCallback);
        promise.onCancel(cancelCallback);
        Assert.assertNull(failureCallback1.getValue());
        Assert.assertNull(failureCallback1.getValue());
        Assert.assertNull(completionCallback.getValue());
        Assert.assertFalse(cancelCallback.isCancelled());

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.failure(new IllegalArgumentException("Test"));
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();

        try {
            promise.get(1, TimeUnit.SECONDS);
            Assert.fail("Exception had been set");
        } catch (IllegalArgumentException ex) {
            // Ignore, expected
        }

        Thread.sleep(200);      // Give the promise time to complete the callbacks
        Assert.assertTrue(failureCallback1.getValue() instanceof IllegalArgumentException);
        Assert.assertTrue("Test".equals(failureCallback1.getValue().getMessage()));
        Assert.assertTrue(failureCallback2.getValue() instanceof IllegalArgumentException);
        Assert.assertTrue("Test".equals(failureCallback2.getValue().getMessage()));
        Assert.assertNull(completionCallback.getValue());
        Assert.assertFalse(cancelCallback.isCancelled());

        // Add another callback after failure
        final FailureCallback failureCallback3 = new FailureCallback();
        promise.onFailure(failureCallback3);
        Assert.assertTrue(failureCallback3.getValue() instanceof IllegalArgumentException);
        Assert.assertTrue("Test".equals(failureCallback3.getValue().getMessage()));
    }

    @Test
    public void testCancelCallback()
        throws Throwable
    {
        final Promise<Integer> promise = PromiseFactory.create();
        final CancelCallback cancelCallback1 = new CancelCallback();
        final CancelCallback cancelCallback2 = new CancelCallback();
        final FailureCallback failureCallback = new FailureCallback();
        final CompletionCallback completionCallback = new CompletionCallback();

        promise.onCancel(cancelCallback1);
        promise.onCancel(cancelCallback2);
        promise.onFailure(failureCallback);
        promise.onComplete(completionCallback);
        Assert.assertFalse(cancelCallback1.isCancelled());
        Assert.assertFalse(cancelCallback2.isCancelled());
        Assert.assertNull(failureCallback.getValue());
        Assert.assertNull(completionCallback.getValue());

        final Thread completeThread = new Thread() {
            @Override
            public void run()
            {
                promise.cancel();
            }
        };

        completeThread.setDaemon(true);
        completeThread.start();

        try {
            promise.get(1, TimeUnit.SECONDS);
            Assert.fail("Promise had been cancelled");
        } catch (CancellationException ex) {
            // Ignore, expected
        }

        Thread.sleep(200);      // Give the promise time to complete the callbacks
        Assert.assertTrue(cancelCallback1.isCancelled());
        Assert.assertTrue(cancelCallback2.isCancelled());
        Assert.assertNull(failureCallback.getValue());
        Assert.assertNull(completionCallback.getValue());

        // Add another callback after cancel
        final CancelCallback cancelCallback3 = new CancelCallback();
        promise.onCancel(cancelCallback3);
        Assert.assertTrue(cancelCallback3.isCancelled());
    }

    @Test
    public void testMapComplete()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final Promise<Long> promise2 = promise.map(new Function<Integer, Long>() {
            @Override
            public Long invoke(final Integer result)
            {
                return new Long(result.longValue() * -1);
            }
        });

        promise.complete(42);
        Assert.assertEquals(Long.valueOf(-42), promise2.get(1, TimeUnit.SECONDS));
    }

    @Test(expected=NullPointerException.class)
    public void testMapFailure()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final Promise<Long> promise2 = promise.map(new Function<Integer, Long>() {
            @Override
            public Long invoke(final Integer result)
            {
                return new Long(result.longValue() * -1);
            }
        });

        promise.failure(new NullPointerException("Test"));
        promise2.get(1, TimeUnit.SECONDS);
    }

    @Test(expected=CancellationException.class)
    public void testMapCancel()
        throws Throwable
    {
        final Promise<Integer> promise = PromiseFactory.create();
        final Promise<Long> promise2 = promise.map(new Function<Integer, Long>() {
            @Override
            public Long invoke(final Integer result)
            {
                return new Long(result.longValue() * -1);
            }
        });

        // Cancels propagate in
        promise2.cancel();
        promise.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void testFlatmap()
        throws Throwable
    {
        final CompletablePromise<List<String>> promise1 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise2 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise3 = PromiseFactory.create();
        final Promise<Set<String>> flattened = promise1.flatMap(new Function<List<String>, Promise<Set<String>>>() {
            @Override
            public Promise<Set<String>> invoke(final List<String> result1)
            {
                return promise2.flatMap(new Function<List<String>, Promise<Set<String>>>() {
                    @Override
                    public Promise<Set<String>> invoke(final List<String> result2)
                    {
                        return promise3.map(new Function<List<String>, Set<String>>() {
                            @Override
                            public Set<String> invoke(final List<String> result3)
                            {
                                Set<String> collected = new TreeSet<String>();

                                collected.addAll(result1);
                                collected.addAll(result2);
                                collected.addAll(result3);

                                return collected;
                            }
                        });
                    }
                });
            }
        });

        Assert.assertFalse(flattened.isCompleted());
        String[] array1 = { "one", "two", "three" };
        promise1.complete(Arrays.asList(array1));
        String[] array2 = { "four", "five", "six" };
        promise2.complete(Arrays.asList(array2));
        String[] array3 = { "seven", "eight", "nine" };
        promise3.complete(Arrays.asList(array3));

        Set<String> allTogether = flattened.get(1, TimeUnit.SECONDS);
        Assert.assertTrue(allTogether.containsAll(Arrays.asList(array1)));
        Assert.assertTrue(allTogether.containsAll(Arrays.asList(array2)));
        Assert.assertTrue(allTogether.containsAll(Arrays.asList(array3)));
    }

    @Test(expected=NullPointerException.class)
    public void testFlatmapFailure()
        throws Throwable
    {
        final CompletablePromise<List<String>> promise1 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise2 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise3 = PromiseFactory.create();
        final Promise<Set<String>> flattened = promise1.flatMap(new Function<List<String>, Promise<Set<String>>>() {
            @Override
            public Promise<Set<String>> invoke(final List<String> result1)
            {
                return promise2.flatMap(new Function<List<String>, Promise<Set<String>>>() {
                    @Override
                    public Promise<Set<String>> invoke(final List<String> result2)
                    {
                        return promise3.map(new Function<List<String>, Set<String>>() {
                            @Override
                            public Set<String> invoke(final List<String> result3)
                            {
                                Set<String> collected = new TreeSet<String>();

                                collected.addAll(result1);
                                collected.addAll(result2);
                                collected.addAll(result3);

                                return collected;
                            }
                        });
                    }
                });
            }
        });

        promise1.failure(new NullPointerException("Test"));
        flattened.get(1, TimeUnit.SECONDS);
    }

    @Test(expected=NullPointerException.class)
    public void testFlatmapFailureInner()
        throws Throwable
    {
        final CompletablePromise<List<String>> promise1 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise2 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise3 = PromiseFactory.create();
        final Promise<Set<String>> flattened = promise1.flatMap(new Function<List<String>, Promise<Set<String>>>() {
            @Override
            public Promise<Set<String>> invoke(final List<String> result1)
            {
                return promise2.flatMap(new Function<List<String>, Promise<Set<String>>>() {
                    @Override
                    public Promise<Set<String>> invoke(final List<String> result2)
                    {
                        return promise3.map(new Function<List<String>, Set<String>>() {
                            @Override
                            public Set<String> invoke(final List<String> result3)
                            {
                                Set<String> collected = new TreeSet<String>();

                                collected.addAll(result1);
                                collected.addAll(result2);
                                collected.addAll(result3);

                                return collected;
                            }
                        });
                    }
                });
            }
        });

        promise2.failure(new NullPointerException("Test"));
        String[] array1 = { "one", "two", "three" };
        promise1.complete(Arrays.asList(array1));
        flattened.get(1, TimeUnit.SECONDS);
    }

    @Test(expected=CancellationException.class)
    public void testFlatmapCancel()
        throws Throwable
    {
        final CompletablePromise<List<String>> promise1 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise2 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise3 = PromiseFactory.create();
        final Promise<Set<String>> flattened = promise1.flatMap(new Function<List<String>, Promise<Set<String>>>() {
            @Override
            public Promise<Set<String>> invoke(final List<String> result1)
            {
                return promise2.flatMap(new Function<List<String>, Promise<Set<String>>>() {
                    @Override
                    public Promise<Set<String>> invoke(final List<String> result2)
                    {
                        return promise3.map(new Function<List<String>, Set<String>>() {
                            @Override
                            public Set<String> invoke(final List<String> result3)
                            {
                                Set<String> collected = new TreeSet<String>();

                                collected.addAll(result1);
                                collected.addAll(result2);
                                collected.addAll(result3);

                                return collected;
                            }
                        });
                    }
                });
            }
        });


        // Cancels propagate in
        flattened.cancel();
        promise1.get(1, TimeUnit.SECONDS);
    }

    @Test
    public void testFlatmapCancelWrapped()
        throws Throwable
    {
        final CompletablePromise<List<String>> promise1 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise2 = PromiseFactory.create();
        final CompletablePromise<List<String>> promise3 = PromiseFactory.create();
        final Promise<Set<String>> flattened = promise1.flatMap(new Function<List<String>, Promise<Set<String>>>() {
            @Override
            public Promise<Set<String>> invoke(final List<String> result1)
            {
                return promise2.flatMap(new Function<List<String>, Promise<Set<String>>>() {
                    @Override
                    public Promise<Set<String>> invoke(final List<String> result2)
                    {
                        return promise3.map(new Function<List<String>, Set<String>>() {
                            @Override
                            public Set<String> invoke(final List<String> result3)
                            {
                                Set<String> collected = new TreeSet<String>();

                                collected.addAll(result1);
                                collected.addAll(result2);
                                collected.addAll(result3);

                                return collected;
                            }
                        });
                    }
                });
            }
        });

        // Cancels propagate in
        promise1.cancel();
        try {
            flattened.get(1, TimeUnit.SECONDS);
            Assert.fail("Succeeded when wrapped promise was cancelled");
        } catch (CancellationException ex) {
            // Expected
        }

        // Make sure flattened doesn't throw an IllegalStateException
        promise2.cancel();
    }

    @Test
    public void testRecover()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final Promise<Integer> promise2 = promise.recover(new Function<Throwable, Integer>() {
            @Override
            public Integer invoke(final Throwable th)
            {
                if (th instanceof ArithmeticException) {
                    return 0;
                }

                throw new IllegalArgumentException();
            }
        });

        promise.failure(new ArithmeticException());
        Assert.assertEquals(Integer.valueOf(0), promise2.get(1, TimeUnit.SECONDS));
    }

    @Test
    public void testRecoverComplete()
        throws Throwable
    {
        final CompletablePromise<Integer> promise = PromiseFactory.create();
        final Promise<Integer> promise2 = promise.recover(new Function<Throwable, Integer>() {
            @Override
            public Integer invoke(final Throwable th)
            {
                if (th instanceof ArithmeticException) {
                    return 0;
                }

                throw new IllegalArgumentException();
            }
        });

        promise.complete(99);
        Assert.assertEquals(Integer.valueOf(99), promise2.get(1, TimeUnit.SECONDS));
    }

    @Test(expected=CancellationException.class)
    public void testRecoverCancel()
        throws Throwable
    {
        final Promise<Integer> promise = PromiseFactory.create();
        final Promise<Integer> promise2 = promise.recover(new Function<Throwable, Integer>() {
            @Override
            public Integer invoke(final Throwable th)
            {
                if (th instanceof ArithmeticException) {
                    return 0;
                }

                throw new IllegalArgumentException();
            }
        });

        // Cancels propagate inside out
        promise2.cancel();
        promise.get(1, TimeUnit.SECONDS);
    }

    private static class CompletionCallback
        implements Callback<Integer>
    {
        private Integer _value = null;

        public Integer getValue()
        {
            return _value;
        }

        @Override
        public void invoke(Integer result)
        {
            _value = result;
        }
    }

    private static class FailureCallback
        implements Callback<Throwable>
    {
        private Throwable _value = null;

        public Throwable getValue()
        {
            return _value;
        }

        @Override
        public void invoke(Throwable result)
        {
            _value = result;
        }
    }

    private static class CancelCallback
        implements Callback<Void>
    {
        private boolean _isCancelled = false;

        public boolean isCancelled()
        {
            return _isCancelled;
        }

        @Override
        public void invoke(Void ignore)
        {
            _isCancelled = true;
        }
    }
}
