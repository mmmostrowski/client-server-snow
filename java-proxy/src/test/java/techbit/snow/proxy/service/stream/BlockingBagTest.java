package techbit.snow.proxy.service.stream;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockingBagTest {

    private BlockingBag<Integer, String> bag;

    @BeforeEach
    void setup() {
        bag = new BlockingBag<>();
    }

    @Test
    public void whenItemInBag_thenItCanBeTaken() throws InterruptedException {
        bag.put(12, "value");

        assertEquals("value", bag.take(12));
    }

    @Test
    public void whenItemInBag_thenItCanBeTakenMultipleTimes() throws InterruptedException {
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
    void whenNoItemInBag_thenBlocksUntilIsAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                String element = bag.take(1);
                assertTick(1);
                Assertions.assertEquals("one", element);
            }

            public void thread2() {
                waitForTick(1);
                bag.put(1, "one");
            }
        });
    }

    @Test
    void whenMultipleItemsInBag_thenBlocksUntilAllAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                waitForTick(1);
                bag.put(1, "one");
                waitForTick(2);
                bag.put(2, "two");
                waitForTick(3);
                bag.put(3, "three");
                waitForTick(4);
                bag.put(4, "four");
                waitForTick(5);
                bag.put(5, "five");
            }

            public void thread2() throws InterruptedException {
                String element1 = bag.take(3);
                String element2 = bag.take(5);
                String element3 = bag.take(1);
                assertTick(5);

                Assertions.assertEquals("three", element1);
                Assertions.assertEquals("five", element2);
                Assertions.assertEquals("one", element3);
            }
        });
    }

    @Test
    void whenTwoItemsInBag_thenTwoThreadsCanAccessThem() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
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

            public void thread3() {
                waitForTick(1);
                bag.put(1, "one");
                waitForTick(2);
                bag.put(2, "two");
            }
        });
    }

    @Test
    void whenTwoItemsInBag_thenTwoThreadsCanAskForSameElement() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                String element = bag.take(1);
                assertTick(1);
                Assertions.assertEquals("one", element);
            }

            public void thread2() throws InterruptedException {
                String element = bag.take(1);
                assertTick(1);
                Assertions.assertEquals("one", element);
            }

            public void thread3() {
                waitForTick(1);
                bag.put(2, "two");
                bag.put(1, "one");
            }
        });
    }

    @Test
    public void whenItemIsRemovedTwice_thenNoErrorOccurs() {
        bag.put(3, "three");
        bag.remove(3);
        bag.remove(3);
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
            public void thread1() throws InterruptedException {
                bag.put(3, "three");
                bag.remove(3);
                bag.take(3);
                assertTick(1);
            }

            public void thread2() {
                waitForTick(1);
                bag.put(3, "other");
            }
        });
    }

    @Test
    public void whenItemIsRemoved_thenCanBeAddedAgain() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                bag.put(3, "three");
                bag.remove(3);
                String element = bag.take(3);

                Assertions.assertEquals("other", element);
            }

            public void thread2() {
                waitForTick(1);
                bag.put(3, "other");
            }
        });
    }

    @Test
    public void whenListeningForRemovalItem_thenProvideEmpty() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                Assertions.assertThrows(NullPointerException.class, () -> bag.take(3));
            }

            public void thread2() {
                waitForTick(1);
                bag.remove(3);
            }
        });
    }

    @Test
    public void whenRemovingAllItems_thenThrowError() throws Throwable {
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
                Assertions.assertThrows(NullPointerException.class, () -> bag.take(3));
            }
        });
    }

    @Test
    public void whenRemovingAllItems_thenTheyCanBeAddedAgain() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
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

            public void thread2() {
                waitForTick(1);
                bag.put(1, "other1");
                bag.put(2, "other2");
                bag.put(3, "other3");
            }
        });
    }

}