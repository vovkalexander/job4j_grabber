package ru.job4j.parser;
import java.util.HashMap;
import java.util.Map;
/**
 * Months.
 * @author Vovk Alexander  vovk.ag747@gmail.com
 * @version $Id$
 * @since 0.1
 */
public class Months {
    /**
     * Field - stores object of Map.
     */
  private   Map<String, String> map = new HashMap<>(12);
    /**
     * Adds value into map and returns.
     * @return filled map by months.
     */
  public Map<String, String> addMonths() {
      map.put("янв", "январь");
      map.put("фев", "февраль");
      map.put("мар", "март");
      map.put("апр", "апрель");
      map.put("июн", "июнь");
      map.put("июл", "июль");
      map.put("авг", "август");
      map.put("сен", "сентябрь");
      map.put("окт", "октябрь");
      map.put("ноя", "ноябрь");
      map.put("дек", "декабрь");
      return map;
  }
}
