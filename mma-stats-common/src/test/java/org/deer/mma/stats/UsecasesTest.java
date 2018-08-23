package org.deer.mma.stats;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.junit.Ignore;
import org.junit.Test;

public class UsecasesTest {

  @Ignore
  @Test
  public void verifySherdogDateformatParseable() {
    final DateTimeFormatter patern = DateTimeFormatter.ofPattern("LLL/dd/yyyy", Locale.ENGLISH);
    final LocalDate date = LocalDate.from(patern.parse("Jul/24/2010"));

    assertEquals(Month.JULY, date.getMonth());
    assertEquals(24, date.getDayOfMonth());
    assertEquals(2010, date.getYear());
  }
}
