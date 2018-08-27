package org.deer.mma.stats.reactor.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.deer.mma.stats.reactor.request.RenderingHttpRequester;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SherdogPageRequestReactor.class, RenderingHttpRequester.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class SherdogPageRequestReactorTest {

  @MockBean(name = "sherdog-page-parse-agent")
  private TaskReactor<String, Document> nextStage;

  @Autowired
  private SherdogPageRequestReactor reactor;

  @Before
  public void start() {
    reactor.start();
    when(nextStage.getName()).thenReturn("mock-next-stage");
  }

  @After
  public void end() throws Exception {
    reactor.close();
  }

  @Test
  public void testSimpleSubmit() throws InterruptedException {
    CountDownLatch finishLock = new CountDownLatch(1);
    reactor.submitTask("http://www.sherdog.com/fighter/Mike-Dolce-17509");
    doAnswer(invocationOnMock -> {
      finishLock.countDown();
      return null;
    }).when(nextStage).submitTask(anyString());

    finishLock.await(30L, TimeUnit.SECONDS);
    assertEquals("Task did not finished", 0, finishLock.getCount());
  }

  @Test
  public void testLimitSubmit() throws InterruptedException {
    CountDownLatch finishLock = new CountDownLatch(5);
    reactor.submitTask("http://www.sherdog.com/fighter/Mike-Dolce-17509");
    reactor.submitTask("http://www.sherdog.com/fighter/JT-Taylor-4429");
    reactor.submitTask("http://www.sherdog.com/fighter/Michael-Riggs-65130");
    reactor.submitTask("http://www.sherdog.com/fighter/Dennis-Olson-20303");
    reactor.submitTask("http://www.sherdog.com/fighter/Roman-Mitichyan-6525");
    doAnswer(invocationOnMock -> {
      finishLock.countDown();
      return null;
    }).when(nextStage).submitTask(anyString());

    finishLock.await(60L, TimeUnit.SECONDS);
    assertEquals("Task did not finished", 0, finishLock.getCount());
  }

  @Test
  public void testOverLimitSubmit() throws InterruptedException {
    CountDownLatch finishLock = new CountDownLatch(6);
    reactor.submitTask("http://www.sherdog.com/fighter/Mike-Dolce-17509");
    reactor.submitTask("http://www.sherdog.com/fighter/JT-Taylor-4429");
    reactor.submitTask("http://www.sherdog.com/fighter/Michael-Riggs-65130");
    reactor.submitTask("http://www.sherdog.com/fighter/Dennis-Olson-20303");
    reactor.submitTask("http://www.sherdog.com/fighter/Roman-Mitichyan-6525");
    reactor.submitTask("http://sherdog.com/fighter/Ronaldo-Souza-8394");
    doAnswer(invocationOnMock -> {
      finishLock.countDown();
      return null;
    }).when(nextStage).submitTask(anyString());

    finishLock.await(60L, TimeUnit.SECONDS);
    assertEquals("Task did not finished", 0, finishLock.getCount());
  }
}