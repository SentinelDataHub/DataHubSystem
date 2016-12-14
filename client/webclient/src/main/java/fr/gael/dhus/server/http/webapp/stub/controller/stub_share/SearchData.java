package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

import java.util.Map;

public class SearchData
{
   private Long id;
   private boolean notify;
   private String value;
   private String complete;
   private Double[][] footprint;
   private Map<String, String> advanced;

   public SearchData ()
   {
   }
   
   public SearchData (Long id, String value, String complete, Map<String, String> advanced, Double[][] footprint, boolean notify)
   {
      this.id = id;
      this.value = value;
      this.complete = complete;
      this.notify = notify;
      this.advanced = advanced;
      this.footprint = footprint;
   }
   
   public Long getId ()
   {
      return id;
   }  

   public boolean isNotify ()
   {
      return notify;
   }

   public void setNotify (boolean notify)
   {
      this.notify = notify;
   }

   public String getValue ()
   {
      return value;
   }

   public void setValue (String value)
   {
      this.value = value;
   }

   public Double[][] getFootprint ()
   {
      return footprint;
   }

   public void setFootprint (Double[][] footprint)
   {
      this.footprint = footprint;
   }

   public Map<String, String> getAdvanced ()
   {
      return advanced;
   }

   public void setAdvanced (Map<String, String> advanced)
   {
      this.advanced = advanced;
   }
   
   public String getComplete ()
   {
      return complete;
   }

   public void setComplete (String complete)
   {
      this.complete = complete;
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof SearchData && ((SearchData) o).id == this.id;
   }
}