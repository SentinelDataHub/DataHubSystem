
package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

import java.util.ArrayList;

public class ProductData
{
   private static String ODATA_PRODUCT_PATH = "odata/v1";
   private Long id;
   private String uuid;
   private String identifier;
   private Double[][][] footprint;
   private ArrayList<String> summary;
   private ArrayList<MetadataIndexData> indexes;
   private boolean thumbnail;
   private boolean quicklook;    
   private String instrument;
   private String productType;
   private String itemClass;
   
   public ProductData () 
   {
   }
   
   public ProductData (Long id, String uuid, String identifier)
   {
      this.id = id;
      this.identifier = identifier;
      this.uuid = uuid;
      /**/
      this.instrument = "";
      this.productType = "";
      /**/

   }

   public Long getId ()
   {
      return id;
   }
   
   public String getUuid ()
   {
      return this.uuid;
   }

   public String getIdentifier ()
   {
      return identifier;
   }

   public void setIdentifier (String identifier)
   {
      this.identifier = identifier;
   }
   
   /**/
   public void setInstrument(String instrument)
   {
          this.instrument=instrument;
   }
      
   public String getInstrument()
   {
          return this.instrument;
   }  
     
   public void setProductType(String productType)
   {
          this.productType=productType;
   }
      
   public String getProductType()
   {
          return this.productType;
   } 

    public String getItemClass() {
        return itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }
   
   
   /**/

   /**
    * Footprint of this product stored as d[0]=latitude, d[1]=longitude. 
    */
   public Double[][][] getFootprint ()
   {
      return footprint;
   }

   public void setFootprint (Double[][][] footprint)
   {
      this.footprint = footprint;
   }
   
   public ArrayList<String> getSummary ()
   {
      return summary;
   }

   public void setSummary (ArrayList<String> summary)
   {
      this.summary = summary;
   }

   public boolean hasThumbnail ()
   {
      return thumbnail;
   }

   public void setHasThumbnail (boolean thumbnail)
   {
      this.thumbnail = thumbnail;
   }
   
   public ArrayList<MetadataIndexData> getIndexes() {
   return indexes;
   }

   public void setIndexes(ArrayList<MetadataIndexData> indexes) {
   this.indexes = indexes;
   }

   public boolean hasQuicklook ()
   {
      return quicklook;
   }

   public void setHasQuicklook (boolean hasQuicklook)
   {
      this.quicklook = hasQuicklook;
   }

   @Override
   public String toString ()
   {
      return identifier;
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof ProductData && ((ProductData) o).id == this.id;
   }
   
   public String getOdataPath (String base_url)
   {
      String slash = "/";
      if (((base_url!=null) && base_url.endsWith ("/")) ||
          ODATA_PRODUCT_PATH.startsWith ("/")) 
         slash="";
      
      return base_url + slash + ODATA_PRODUCT_PATH + "/Products('" + this.uuid + "')";
   }
   
   public String getOdataDownaloadPath (String base_url)
   {
      return getOdataPath (base_url) + "/$value";
   }
   
   public String getOdataQuicklookPath (String base_url)
   {
      return getOdataPath (base_url) + "/Products('Quicklook')/$value";
   }
   
   public String getOdataThumbnailPath (String base_url)
   {
      return getOdataPath (base_url) + "/Products('Thumbnail')/$value";
   }
}