package org.jamon.render.html;

import org.jamon.render.html.Select;

import junit.framework.TestCase;

public class SingleSelectTest extends TestCase
{
   private static class Data
   {
      public Data(int p_x)
      {
         x = p_x;
      }
      int x;
   }

   private static class Renderable
   {
      public Renderable(char p_c)
      {
         c  = p_c;
      }
      char c;
   }

   public static class TestItem extends SingleSelect.Item<Renderable>
   {
      private int m_x;

      public TestItem(int p_x)
      {
         m_x = p_x;
      }
      public Renderable getRenderable()
      {
         return new Renderable((char) m_x);
      }

      public String getValue()
      {
         return Integer.toString(m_x);
      }
   }

   public static class TestItemMaker
      implements AbstractSelect.ItemMaker<Data, Renderable>
   {
      public Select.Item<Renderable> makeItem(final Data p_data)
      {
         return new TestItem(p_data.x);
      }
   }

   public void testConstructFromArray()
   {
      SingleSelect<Renderable> select = new SingleSelect<Renderable>(
         "name", "3", new Data[] { new Data(1), new Data(3), new Data(5) },
         new TestItemMaker());
      verifySelect(select);
   }

   public void testConstructDirect()
   {
      SingleSelect<Renderable> select = new SingleSelect<Renderable>(
            "name",
            "3",
            new TestItem[] { new TestItem(1), new TestItem(3), new TestItem(5) }
            );
      verifySelect(select);
   }

   private void verifySelect(SingleSelect<Renderable> select)
   {
      assertEquals(3, select.getItems().length);

      assertEquals((char) 1, select.getItems()[0].getRenderable().c);
      assertEquals("1", select.getItems()[0].getValue());
      assertFalse(select.getItems()[0].isSelected());

      assertEquals((char) 3, select.getItems()[1].getRenderable().c);
      assertEquals("3", select.getItems()[1].getValue());
      assertTrue(select.getItems()[1].isSelected());
   }
}