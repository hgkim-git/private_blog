package io.github.hgkimer.privateblog.service;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarkdownService {

  private final Parser parser;
  private final HtmlRenderer renderer;
  private final Safelist safelist;

  public String convertToHtml(String markdown) {
    if (markdown == null || markdown.isBlank()) {
      return "";
    }
    Document document = parser.parse(markdown);
    String html = renderer.render(document);
    log.debug("Converted markdown to html: {}", html);
    return Jsoup.clean(html, safelist);
  }

  public String getTocHtml(String html) {
    Element ul = new Element("ul").addClass("toc-list");
    org.jsoup.nodes.Document document = Jsoup.parse(html);

    Elements elems = document.select("h2");
    elems.forEach(elem -> {
      String toc = elem.id();
      String text = elem.text();
      Element li = new Element("li").addClass("toc-item");
      Element anchor = new Element("a").addClass("toc-link")
          .attr("href", "#" + toc).text(text);
      li.appendChild(anchor);
      ul.appendChild(li);
    });

    return ul.html();
  }
}
