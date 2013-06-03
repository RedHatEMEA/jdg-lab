package org.jboss.jdf.examples.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.jboss.jdf.example.ticketmonster.model.TicketPrice;
import org.jboss.jdf.example.ticketmonster.model.Section;
import org.jboss.jdf.example.ticketmonster.model.Show;
import org.jboss.jdf.example.ticketmonster.model.TicketCategory;

/**
 * Backing bean for TicketPrice entities.
 * <p>
 * This class provides CRUD functionality for all TicketPrice entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class TicketPriceBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving TicketPrice entities
    */

   private Long id;

   public Long getId()
   {
      return this.id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   private TicketPrice ticketPrice;

   public TicketPrice getTicketPrice()
   {
      return this.ticketPrice;
   }

   @Inject
   private Conversation conversation;

   @PersistenceContext(type = PersistenceContextType.EXTENDED)
   private EntityManager entityManager;

   public String create()
   {

      this.conversation.begin();
      return "create?faces-redirect=true";
   }

   public void retrieve()
   {

      if (FacesContext.getCurrentInstance().isPostback())
      {
         return;
      }

      if (this.conversation.isTransient())
      {
         this.conversation.begin();
      }

      if (this.id == null)
      {
         this.ticketPrice = this.example;
      }
      else
      {
         this.ticketPrice = findById(getId());
      }
   }

   public TicketPrice findById(Long id)
   {

      return this.entityManager.find(TicketPrice.class, id);
   }

   /*
    * Support updating and deleting TicketPrice entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.ticketPrice);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.ticketPrice);
            return "view?faces-redirect=true&id=" + this.ticketPrice.getId();
         }
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   public String delete()
   {
      this.conversation.end();

      try
      {
         this.entityManager.remove(findById(getId()));
         this.entityManager.flush();
         return "search?faces-redirect=true";
      }
      catch (Exception e)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.getMessage()));
         return null;
      }
   }

   /*
    * Support searching TicketPrice entities with pagination
    */

   private int page;
   private long count;
   private List<TicketPrice> pageItems;

   private TicketPrice example = new TicketPrice();

   public int getPage()
   {
      return this.page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public int getPageSize()
   {
      return 10;
   }

   public TicketPrice getExample()
   {
      return this.example;
   }

   public void setExample(TicketPrice example)
   {
      this.example = example;
   }

   public void search()
   {
      this.page = 0;
   }

   public void paginate()
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

      // Populate this.count

      CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
      Root<TicketPrice> root = countCriteria.from(TicketPrice.class);
      countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<TicketPrice> criteria = builder.createQuery(TicketPrice.class);
      root = criteria.from(TicketPrice.class);
      TypedQuery<TicketPrice> query = this.entityManager.createQuery(criteria.select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<TicketPrice> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      Show show = this.example.getShow();
      if (show != null)
      {
         predicatesList.add(builder.equal(root.get("show"), show));
      }
      Section section = this.example.getSection();
      if (section != null)
      {
         predicatesList.add(builder.equal(root.get("section"), section));
      }
      TicketCategory ticketCategory = this.example.getTicketCategory();
      if (ticketCategory != null)
      {
         predicatesList.add(builder.equal(root.get("ticketCategory"), ticketCategory));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<TicketPrice> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back TicketPrice entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<TicketPrice> getAll()
   {

      CriteriaQuery<TicketPrice> criteria = this.entityManager.getCriteriaBuilder().createQuery(TicketPrice.class);
      return this.entityManager.createQuery(criteria.select(criteria.from(TicketPrice.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final TicketPriceBean ejbProxy = this.sessionContext.getBusinessObject(TicketPriceBean.class);

      return new Converter()
      {

         @Override
         public Object getAsObject(FacesContext context, UIComponent component, String value)
         {

            return ejbProxy.findById(Long.valueOf(value));
         }

         @Override
         public String getAsString(FacesContext context, UIComponent component, Object value)
         {

            if (value == null)
            {
               return "";
            }

            return String.valueOf(((TicketPrice) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private TicketPrice add = new TicketPrice();

   public TicketPrice getAdd()
   {
      return this.add;
   }

   public TicketPrice getAdded()
   {
      TicketPrice added = this.add;
      this.add = new TicketPrice();
      return added;
   }
}