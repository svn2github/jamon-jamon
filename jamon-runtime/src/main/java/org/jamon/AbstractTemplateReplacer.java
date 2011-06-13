package org.jamon;

import org.jamon.AbstractTemplateProxy.ImplData;
import org.jamon.AbstractTemplateProxy.ImplDataCompatible;

/**
 * A base class for classes wishing to define a {@code TemplateReplacer}. Implementors need only
 * define {@link #findReplacement(Class, Object)}
 */
public abstract class AbstractTemplateReplacer implements TemplateReplacer {

  public AbstractTemplateProxy getReplacement(AbstractTemplateProxy p_proxy, Object p_jamonContext) {
    Class<? extends AbstractTemplateProxy> redirect =
      findReplacement(p_proxy.getClass(), p_jamonContext);
    if (redirect != null) {
        try {
          AbstractTemplateProxy replacedProxy =
            redirect.getConstructor(new Class [] { TemplateManager.class })
            .newInstance(new Object [] { p_proxy.getTemplateManager() });
          @SuppressWarnings("unchecked")
          ImplDataCompatible<ImplData> replacedImplData =
            (ImplDataCompatible<ImplData>)replacedProxy.getImplData();
          replacedImplData.populateFrom(p_proxy.getImplData());
          return replacedProxy;
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
      return p_proxy;
    }
  }

  /**
   * Find an appropriate replacement for a template, if there is one.
   *
   * @param p_proxyClass the class to find a replacement for.
   * @param p_jamonContext the jamonContext
   * @return the proxy class for the template which will serve as a replacement, or {@code null}
   * if there is to be no replacement performed.
   */
  protected abstract Class<? extends AbstractTemplateProxy> findReplacement(
    Class<?> p_proxyClass,
    Object p_jamonContext);

}
