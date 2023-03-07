package techbit.snow.proxy.collection;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlockingBagTest {

    private BlockingBag<Integer, String> bag;

    @BeforeEach
    void setup() {
        bag = new BlockingBag<>();
    }

    @Test
    void whenConsumerAskNonExistingElement_waitsOnceIsAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                String element = bag.take(1);
                assertTick(1);
                Assertions.assertEquals("one", element);
            }

            public void thread2() throws InterruptedException {
                waitForTick(1);
                bag.put(1, "one");
            }
        });
    }

    @Test
    void whenProducerPuttingElements_thereIsNoWaiting() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                bag.put(1, "one");
                bag.put(2, "two");
                assertTick(0);
            }
        });
    }

    @Test
    void whenProducerPuttingElementsBeforeConsumer_consumerCanConsume() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() {
                bag.put(1, "one");
                bag.put(2, "two");
                bag.put(3, "three");
                bag.put(4, "four");
                bag.put(5, "five");
            }

            public void thread2() throws InterruptedException {
                waitForTick(1);
                String element = bag.take(3);

                Assertions.assertEquals("three", element);
            }
        });
    }

    @Test
    void whenProducerPuttingMultipleElements_consumerIgnoresIrrelevantElements() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                waitForTick(1);
                bag.put(1, "one");
                bag.put(2, "two");
                bag.put(3, "three");
                bag.put(4, "four");
                bag.put(5, "five");
            }

            public void thread2()  throws InterruptedException {
                String element = bag.take(3);
                assertTick(1);
                Assertions.assertEquals("three", element);
            }
        });
    }

    @Test
    void whenTwoConsumersAskingForDifferentElements_bothWaitOnceAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                String element = bag.take(1);
                assertTick(2);
                Assertions.assertEquals("one", element);
            }

            public void thread2() throws InterruptedException {
                String element = bag.take(2);
                assertTick(2);
                Assertions.assertEquals("two", element);
            }

            public void thread3() throws InterruptedException {
                waitForTick(2);
                bag.put(1, "one");
                bag.put(2, "two");
            }
        });
    }

    @Test
    void whenTwoConsumersAskingForSameElement_bothWaitOnceAvailable() throws Throwable {
        TestFramework.runOnce(new MultithreadedTestCase() {
            public void thread1() throws InterruptedException {
                String element = bag.take(1);
                assertTick(2);
                Assertions.assertEquals("one", element);
            }

            public void thread2() throws InterruptedException {
                String element = bag.take(1);
                assertTick(2);
                Assertions.assertEquals("one", element);
            }

            public void thread3() throws InterruptedException {
                waitForTick(2);
                bag.put(2, "two");
                bag.put(1, "one");
            }
        });
    }

}