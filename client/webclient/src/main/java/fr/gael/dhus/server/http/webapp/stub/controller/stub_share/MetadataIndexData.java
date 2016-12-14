package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

import java.util.ArrayList;
import java.util.List;

public class MetadataIndexData
{
   private String name;
   private String value;
   private List<MetadataIndexData> children;

   public MetadataIndexData ()
   {
   }

   public MetadataIndexData (String name, String value)
   {
      this.name = name;
      this.value = value;
   }

   public List<MetadataIndexData> getChildren()
   {
      return children;
   }

   public void addChild(MetadataIndexData child)
   {
      if (children == null)
      {
         children = new ArrayList<MetadataIndexData>();
      }
      children.add(child);
   }

   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getValue() {
      return value;
   }
   public void setValue(String value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object o)
   {
      return o instanceof MetadataIndexData && ((MetadataIndexData)o).getName().equals(this.getName());
   }

   @Override
   public String toString()
   {
      String res = name;
      if (value != null)
      {
         res += " : "+value;
      }
      return res;
   }
}
