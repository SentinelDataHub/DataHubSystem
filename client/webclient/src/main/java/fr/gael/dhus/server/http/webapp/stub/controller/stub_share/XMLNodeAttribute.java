package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

public class XMLNodeAttribute
{
   private String name;
   private String value;
   
   public XMLNodeAttribute (String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   public String getName ()
   {
      return name;
   }

   public void setName (String name)
   {
      this.name = name;
   }

   public String getValue ()
   {
      return value;
   }

   public void setValue (String value)
   {
      this.value = value;
   }
}