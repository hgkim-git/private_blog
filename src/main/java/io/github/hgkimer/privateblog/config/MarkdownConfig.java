package io.github.hgkimer.privateblog.config;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import io.github.hgkimer.privateblog.web.flexmark.CustomAttributeProvider;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarkdownConfig {


  @Bean
  public MutableDataSet flexmarkOptions() {
    MutableDataSet options = new MutableDataSet();

    MutableDataSet extensions = new MutableDataSet();
    extensions.set(Parser.EXTENSIONS, List.of(
        TablesExtension.create(),
        TocExtension.create(),
        TypographicExtension.create()
    ));
    options.setAll(extensions);

    MutableDataSet tocOptions = new MutableDataSet();
    options.setAll(tocOptions);
    return options;
  }

  @Bean
  public Parser parser(MutableDataSet flexmarkOptions) {
    return Parser.builder(flexmarkOptions).build();
  }

  @Bean
  public HtmlRenderer htmlRenderer(MutableDataSet flexmarkOptions) {
    return HtmlRenderer.builder(flexmarkOptions).attributeProviderFactory(
        CustomAttributeProvider.Factory()).build();
  }

}
