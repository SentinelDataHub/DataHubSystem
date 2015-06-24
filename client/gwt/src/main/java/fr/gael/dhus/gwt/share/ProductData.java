/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.gwt.share;

import java.util.ArrayList;


import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.IsSerializable;


public class ProductData implements IsSerializable
{
   private static String ODATA_PRODUCT_PATH = "odata/v1";
   private Long id;
   private String uuid;
   private String identifier;
   private Double[][] footprint;
   private ArrayList<String> summary;
   private ArrayList<MetadataIndexData> indexes;
   private boolean thumbnail;
   private boolean quicklook;   
   
   public ProductData () 
   {
   }
   
   public ProductData (Long id, String uuid, String identifier)
   {
      this.id = id;
      this.identifier = identifier;
      this.uuid = uuid;
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

   /**
    * Footprint of this product stored as d[0]=latitude, d[1]=longitude. 
    */
   public Double[][] getFootprint ()
   {
      return footprint;
   }

   public void setFootprint (Double[][] footprint)
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
   
   
   public static JavaScriptObject getJsFootprintLayer (Double[][] footprint)
   {
      if (footprint != null && footprint.length > 0)
      {                           
         Point[] pts = new Point[footprint.length];
         int i = 0;
         for (Double[] d : footprint)
         {                              
            // x=longitude
            // y=latitude
            pts[i] = new Point (d[0], d[1]);
            i++;
         }
         LinearRing fp = new LinearRing (pts);
         Polygon poly = new Polygon (new LinearRing[] { fp });
         return new VectorFeature (poly).getJSObject ();
      }
      else 
         return null;
   }
   

}
