package fr.gael.dhus.server.http.valve;

import fr.gael.dhus.server.http.valve.AccessInformation.Status;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.AbstractPolicy;
import net.sf.ehcache.store.Policy;

/**
 * This class override a parent policy to forbid eviction of pending access
 * entries into the cache.
 * Thanks to this policy, pending long requests (such as download) will be kept
 * into the cache to be able to count the number of pending connecitons. 
 */
public class NoPendingEvictionPolicy extends AbstractPolicy
{
   Policy policy;

   public NoPendingEvictionPolicy(Policy parent_policy)
   {
      this.policy = parent_policy;
   }

   @Override
   public String getName()
   {
      return "NOT-PENDING";
   }

   @Override
   public boolean compare(Element element1, Element element2)
   {
      if ((element1.getObjectValue() instanceof AccessInformation) &&
          (element2.getObjectValue() instanceof AccessInformation))
      {
         boolean is_pending1 = false;
         boolean is_pending2 = false;
         AccessInformation ai1 = (AccessInformation)element1.getObjectValue();
         AccessInformation ai2 = (AccessInformation)element2.getObjectValue();

         if (Status.PENDING.equals(ai1.getConnectionStatus().getStatus()))
         {
            is_pending1 = true;
         }
         if (Status.PENDING.equals(ai2.getConnectionStatus().getStatus()))
         {
            is_pending2 = true;
         }

         // true if the second element is preferable for eviction to the
         // first element under the policy: PENDING shall not be evicted.
         if (is_pending1 != is_pending2)
         {
            return is_pending1 && !is_pending2;
         }
         // If both or none are pending, let the default policy manage the case.
      }
      return policy.compare(element1, element2);
   }

}
