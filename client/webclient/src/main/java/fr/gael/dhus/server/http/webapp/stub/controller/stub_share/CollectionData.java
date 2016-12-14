package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

import java.util.ArrayList;
import java.util.List;



public class CollectionData
{
   private String uuid;
   private String name;
   private String description;
   private List<Long> productIds;
   private List<Long> addedIds;
   private List<Long> removedIds;

   public CollectionData ()
   {
   }

   public CollectionData (String uuid, String name, String description
      )
   {
      this.uuid = uuid;
      this.name = name;
      this.description = description;
      
   }

   public void setUUID (String uuid)
   {
      this.uuid = uuid;
   }

   public String getUUID ()
   {
      return uuid;
   }

   public String getName ()
   {
      return name;
   }

   public void setName (String name)
   {
      this.name = name;
   }

   public String getDescription ()
   {
      return description;
   }

   public void setDescription (String description)
   {
      this.description = description;
   }

   public List<Long> getProductIds ()
   {
      return productIds;
   }

   public void setProductIds (List<Long> productIds)
   {
      this.productIds = productIds;
   }
   
   public void addProduct (Long pid)
   {
      checkLists();
      if (removedIds.contains (pid))
      {
         removedIds.remove (pid);
         return;
      }
      if (addedIds.contains (pid) || productIds.contains (pid))
      {
         return;
      }
      addedIds.add (pid);      
   }
   
   public void removeProduct (Long pid)
   {
      checkLists();
      if (addedIds.contains (pid))
      {
         addedIds.remove (pid);
         return;
      }
      if (removedIds.contains (pid) || !productIds.contains (pid))
      {
         return;
      }      
      removedIds.add (pid);
   }
   
   public void addProducts (Long[] pid)
   {
      for (Long id : pid)
      {
         addProduct(id);
      }
   }
   
   public void removeProducts (Long[] pid)
   {
      for (Long id : pid)
      {
         removeProduct(id);
      }
   }
   
   private void checkLists()
   {
      if (productIds == null)
      {
         this.productIds = new ArrayList<Long> ();
      }
      if (addedIds == null)
      {
         this.addedIds = new ArrayList<Long> ();
      }
      if (removedIds == null)
      {
         this.removedIds = new ArrayList<Long> ();
      }
   }
   
   public List<Long> getAddedIds ()
   {
      return addedIds;
   }

   public void setAddedIds (List<Long> addedIds)
   {
      this.addedIds = addedIds;
   }

   public List<Long> getRemovedIds ()
   {
      return removedIds;
   }

   public void setRemovedIds (List<Long> removedIds)
   {
      this.removedIds = removedIds;
   }
   
   public boolean contains(Long pid)
   {
      boolean added = addedIds != null && addedIds.contains (pid);
      boolean removed = removedIds != null && removedIds.contains (pid);
      boolean base = productIds != null && productIds.contains (pid);
      
      return added || (base && !removed);
   }

   @Override
   public String toString ()
   {
      return name;
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof CollectionData && ((CollectionData) o).uuid == this.uuid;
   }

   public CollectionData copy ()
   {
      CollectionData copy = new CollectionData(uuid, name, description);
      copy.setProductIds (productIds);
      copy.setAddedIds (addedIds);
      copy.setRemovedIds (removedIds);
      return copy;
   }

}