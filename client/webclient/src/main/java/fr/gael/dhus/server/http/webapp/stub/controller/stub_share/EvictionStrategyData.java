package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

public class EvictionStrategyData
{
   private String id;
   private String description;

   public EvictionStrategyData()
   {
   }
   
   public EvictionStrategyData(String id, String description)
   {
      this.id = id;
      this.description = description;
   }
   
   public String getId ()
   {
      return id;
   }
   public void setId (String id)
   {
      this.id = id;
   }
   public String getDescription ()
   {
      return description;
   }
   public void setDescription (String description)
   {
      this.description = description;
   }
}
