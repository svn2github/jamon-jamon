package org.jamon;

import org.jamon.AbstractTemplateProxy.ImplData;
import org.jamon.AbstractTemplateProxy.ImplDataCompatible;
import org.jamon.AbstractTemplateProxy.Intf;

/**
 * A {@code TemplateManager} which can replace templates with others that declare themselves to
 * be replacement candidates via the {@code <%replacement>} tag.
 */
public abstract class AbstractReplacingTemplateManager extends BasicTemplateManager {
  @Override
  public Intf constructImpl(AbstractTemplateProxy p_proxy) {
    Class<? extends AbstractTemplateProxy> redirect = findReplacement(p_proxy.getClass());
    if (redirect != null) {
        try {
          AbstractTemplateProxy redirectedProxy =
            redirect.getConstructor(new Class [] { TemplateManager.class })
            .newInstance(new Object [] { this });
          @SuppressWarnings("unchecked")
          ImplDataCompatible<ImplData> redirectedImplData =
            (ImplDataCompatible<ImplData>)redirectedProxy.getImplData();
          redirectedImplData.populateFrom(p_proxy.getImplData());
          return redirectedProxy.constructImpl();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    else {
      return super.constructImpl(p_proxy);
    }
  }

  /**
   * Find an appropriate replacement for a template, if there is one.
   *
   * @param p_proxyClass the class to find a replacement for.
   * @return the proxy class for the template which will serve as a replacement, or {@code null}
   * if there is to be no replacement performed.
   */
  protected abstract Class<? extends AbstractTemplateProxy> findReplacement(Class<?> p_proxyClass);
}
