package io.github.hgkimer.privateblog.web.flexmark;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.html.MutableAttributes;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public class CustomAttributeProvider implements AttributeProvider {


  public static AttributeProviderFactory Factory() {
    return new IndependentAttributeProviderFactory() {
      @NotNull
      @Override
      public AttributeProvider apply(@NotNull LinkResolverContext context) {
        return new CustomAttributeProvider();
      }
    };
  }

  @Override
  public void setAttributes(@NotNull Node node, @NotNull AttributablePart part,
      @NotNull MutableAttributes attributes) {
    if (node instanceof Heading) {
      attributes.replaceValue("id", UUID.randomUUID().toString());
    }
  }

}
