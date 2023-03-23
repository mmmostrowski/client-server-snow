package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
class BlockingBagTest {

    private BlockingBag<Integer, String> bag;

    @BeforeEach
    void setup() {
        bag = new BlockingBag<>();
    }

    
    @Test
    public void whenItemIsInBag_thenItCanBeTaken() throws InterruptedException {
        bag.put(12, "value");

        assertEquals("value", bag.take(12));
    }

    @Test
    public void whenItemIsInBag_thenItCanBeTakenMultipleTimes() throws InterruptedException {
        bag.put(12, "value");

        assertEquals("value", bag.take(12));
        assertEquals("value", bag.take(12));
        assertEquals("value", bag.take(12));
    }

    @Test
    void whenPuttingTwoItemsToBag_thenNoBlockingOccurs() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1()  {
                bag.put(1, "one");
                bag.put(2, "two");
                assertTick(0);
            }
        });
    }

    @Test
    public void whenSameItemIsPutTwice_thenRecentValueIsProvided() throws Throwable {
        bag.put(3, "three");
        bag.put(3, "other");

        assertEquals("other", bag.take(3));
    }

    @Test
    void whenNoItemInBag_thenBlockUntilIsAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void threadProducer() {
                waitForTick(1);
                bag.put(1, "one");
            }

            public void thread1() throws InterruptedException {
                String element = bag.take(1);
                assertTick(1);
                Assertions.assertEquals("one", element);
            }
        });
    }

    @Test
    void whenMultipleItemsInBag_thenBlockEachUntilIsAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void threadProducer() {
                waitForTick(1);
                bag.put(1, "one");
                bag.put(2, "two");
                bag.put(3, "three");
                waitForTick(2);
                bag.put(4, "four");
                bag.put(5, "five");
            }

            public void thread1() throws InterruptedException {
                String element3 = bag.take(3);
                assertTick(1);
                String element5 = bag.take(5);
                String element1 = bag.take(1);
                assertTick(2);

                Assertions.assertEquals("three", element3);
                Assertions.assertEquals("five", element5);
                Assertions.assertEquals("one", element1);
            }
        });
    }

    @Test
    void whenTwoItemsInBag_thenTwoThreadsCanAccessThem() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void threadProducer() {
                waitForTick(1);
                bag.put(1, "one");
                waitForTick(2);
                bag.put(2, "two");
            }

            public void thread1() throws InterruptedException {
                String element1 = bag.take(1);
                assertTick(1);
                String element2 = bag.take(2);
                assertTick(2);
                Assertions.assertEquals("one", element1);
                Assertions.assertEquals("two", element2);
            }

            public void thread2() throws InterruptedException {
                String element1 = bag.take(2);
                assertTick(2);
                String element2 = bag.take(1);
                assertTick(2);
                Assertions.assertEquals("two", element1);
                Assertions.assertEquals("one", element2);
            }
        });
    }

    @Test
    void whenTwoItemsInBag_thenMultipleThreadsCanAskForSameElement() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void threadProducer() {
                waitForTick(1);
                bag.put(2, "two");
                bag.put(1, "one");
            }

            public void thread1() throws InterruptedException {
                test();
            }

            public void thread2() throws InterruptedException {
                test();
            }

            public void thread3() throws InterruptedException {
                test();
            }

            private void test() throws InterruptedException {
                String element = bag.take(1);
                assertTick(1);
                Assertions.assertEquals("one", element);
            }
        });
    }

    @Test
    public void whenItemIsRemovedTwice_thenNoErrorOccurs() {
        bag.put(3, "three");
        assertDoesNotThrow(() -> bag.remove(3));
        assertDoesNotThrow(() -> bag.remove(3));
    }

    @Test
    public void whenItemIsRemovedAndAddedAgain_thenIsAvailableAgainWithNoBlocking() throws Throwable {
        bag.put(3, "three");
        bag.remove(3);
        bag.put(3, "new");
        assertEquals(bag.take(3), "new");
    }

    @Test
    public void whenItemIsRemoved_thenBagIsBlockedDuringRetake() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                waitForTick(1);
                bag.put(3, "other");
            }

            public void thread2() throws InterruptedException {
                bag.put(3, "three");
                bag.remove(3);
                bag.take(3);
                assertTick(1);
            }
        });
    }

    @Test
    public void whenItemIsRemoved_thenCanBeAddedAgain() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                waitForTick(1);
                bag.put(3, "other");
            }

            public void thread2() throws InterruptedException {
                bag.put(3, "three");
                bag.remove(3);
                String element = bag.take(3);

                Assertions.assertEquals("other", element);
            }
        });
    }

    @Test
    public void whenListeningForRemovedItem_thenThrowNPE() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                waitForTick(1);
                bag.remove(3);
            }

            public void thread2() {
                Assertions.assertThrows(NullPointerException.class, () -> bag.take(3));
            }
        });
    }

    @Test
    public void whenRemovingAllItems_thenThrowNPE() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                waitForTick(1);
                bag.removeAll();
            }

            public void thread2() {
                Assertions.assertThrows(NullPointerException.class, () -> bag.take(1));
            }

            public void thread3() {
                Assertions.assertThrows(NullPointerException.class, () -> bag.take(2));
            }

            public void thread4() {
                Assertions.assertThrows(NullPointerException.class, () -> bag.take(33));
            }
        });
    }

    @Test
    public void whenRemovingAllItems_thenTheyCanBeAddedAgain() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                waitForTick(1);
                bag.put(1, "other1");
                bag.put(2, "other2");
                bag.put(3, "other3");
            }

            public void thread2() throws InterruptedException {
                bag.put(1, "one");
                bag.put(2, "two");
                bag.put(3, "three");
                bag.removeAll();
                String element1 = bag.take(1);
                String element2 = bag.take(2);
                String element3 = bag.take(3);

                Assertions.assertEquals("other1", element1);
                Assertions.assertEquals("other2", element2);
                Assertions.assertEquals("other3", element3);
            }
        });
    }

}