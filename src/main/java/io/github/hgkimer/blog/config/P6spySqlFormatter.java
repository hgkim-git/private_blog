package io.github.hgkimer.blog.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;

public class P6spySqlFormatter implements MessageFormattingStrategy {

  private static final BasicFormatterImpl formatter = new BasicFormatterImpl();

  @Override
  public String formatMessage(int connectionId, String now, long elapsed, String category,
      String prepared, String sql, String url) {
    if (sql == null || sql.isBlank()) {
      return "";
    }
    String formattedSql =
        Category.STATEMENT.getName().equals(category) ? formatter.format(sql) : sql;
    return String.format("[%dms] %s%s", elapsed, category, formattedSql);
  }
}
