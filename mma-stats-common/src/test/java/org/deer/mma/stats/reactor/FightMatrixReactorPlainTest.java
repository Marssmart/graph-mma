package org.deer.mma.stats.reactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class FightMatrixReactorPlainTest {

  @Test
  public void parseFighterLinks() throws IOException {
    URL resource = Resources.getResource("fight-matrix-example-payload.html");
    String content = Resources.toString(resource, StandardCharsets.UTF_8);

    Set<String> set = FightMatrixReactor.parseFightMatrixLinks(content);

    assertEquals(3, set.size());
    assertTrue(
        set.contains("http://www.fightmatrix.com/fighter-profile/Rory+MacDonald/25924/"));
    assertTrue(set.contains("http://www.fightmatrix.com/fighter-profile/Uriah+Hall/26116/"));
    assertTrue(
        set.contains("http://www.fightmatrix.com/fighter-profile/Rafael+Carvalho/90597/"));
  }

  @Test
  public void parseSherdogLink() throws IOException {
    URL resource = Resources.getResource("fight-matrix-example-payload.html");
    String content = Resources.toString(resource, StandardCharsets.UTF_8);

    Optional<String> sherdogLink = FightMatrixReactor.parseSherdogLink(content);
    assertTrue(sherdogLink.isPresent());
    assertEquals("http://www.sherdog.com/fighter/Gegard-Mousasi-7466", sherdogLink.get());
  }

  @Test
  public void parseFighterName() {
    Optional<String> rory = FightMatrixReactor
        .parseFullName("http://www.fightmatrix.com/fighter-profile/Rory+MacDonald/25924/");
    Optional<String> uriah = FightMatrixReactor
        .parseFullName("http://www.fightmatrix.com/fighter-profile/Uriah/26116/");
    Optional<String> rafael = FightMatrixReactor
        .parseFullName("http://www.fightmatrix.com/fighter-profile/Rafael+Carvalho+Monina/90597/");

    assertEquals("Rory MacDonald", rory.get());
    assertEquals("Uriah", uriah.get());
    assertEquals("Rafael Carvalho Monina", rafael.get());
  }

}
