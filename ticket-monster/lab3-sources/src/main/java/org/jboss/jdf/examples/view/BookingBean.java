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

import org.jboss.jdf.example.ticketmonster.model.Booking;
import org.jboss.jdf.example.ticketmonster.model.Performance;

/**
 * Backing bean for Booking entities.
 * <p>
 * This class provides CRUD functionality for all Booking entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD framework or
 * custom base class.
 */

@Named
@Stateful
@ConversationScoped
public class BookingBean implements Serializable
{

   private static final long serialVersionUID = 1L;

   /*
    * Support creating and retrieving Booking entities
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

   private Booking booking;

   public Booking getBooking()
   {
      return this.booking;
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
         this.booking = this.example;
      }
      else
      {
         this.booking = findById(getId());
      }
   }

   public Booking findById(Long id)
   {

      return this.entityManager.find(Booking.class, id);
   }

   /*
    * Support updating and deleting Booking entities
    */

   public String update()
   {
      this.conversation.end();

      try
      {
         if (this.id == null)
         {
            this.entityManager.persist(this.booking);
            return "search?faces-redirect=true";
         }
         else
         {
            this.entityManager.merge(this.booking);
            return "view?faces-redirect=true&id=" + this.booking.getId();
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
    * Support searching Booking entities with pagination
    */

   private int page;
   private long count;
   private List<Booking> pageItems;

   private Booking example = new Booking();

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

   public Booking getExample()
   {
      return this.example;
   }

   public void setExample(Booking example)
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
      Root<Booking> root = countCriteria.from(Booking.class);
      countCriteria = countCriteria.select(builder.count(root)).where(getSearchPredicates(root));
      this.count = this.entityManager.createQuery(countCriteria).getSingleResult();

      // Populate this.pageItems

      CriteriaQuery<Booking> criteria = builder.createQuery(Booking.class);
      root = criteria.from(Booking.class);
      TypedQuery<Booking> query = this.entityManager.createQuery(criteria.select(root).where(getSearchPredicates(root)));
      query.setFirstResult(this.page * getPageSize()).setMaxResults(getPageSize());
      this.pageItems = query.getResultList();
   }

   private Predicate[] getSearchPredicates(Root<Booking> root)
   {

      CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
      List<Predicate> predicatesList = new ArrayList<Predicate>();

      String cancellationCode = this.example.getCancellationCode();
      if (cancellationCode != null && !"".equals(cancellationCode))
      {
         predicatesList.add(builder.like(root.<String> get("cancellationCode"), '%' + cancellationCode + '%'));
      }
      String contactEmail = this.example.getContactEmail();
      if (contactEmail != null && !"".equals(contactEmail))
      {
         predicatesList.add(builder.like(root.<String> get("contactEmail"), '%' + contactEmail + '%'));
      }
      Performance performance = this.example.getPerformance();
      if (performance != null)
      {
         predicatesList.add(builder.equal(root.get("performance"), performance));
      }

      return predicatesList.toArray(new Predicate[predicatesList.size()]);
   }

   public List<Booking> getPageItems()
   {
      return this.pageItems;
   }

   public long getCount()
   {
      return this.count;
   }

   /*
    * Support listing and POSTing back Booking entities (e.g. from inside an
    * HtmlSelectOneMenu)
    */

   public List<Booking> getAll()
   {

      CriteriaQuery<Booking> criteria = this.entityManager.getCriteriaBuilder().createQuery(Booking.class);
      return this.entityManager.createQuery(criteria.select(criteria.from(Booking.class))).getResultList();
   }

   @Resource
   private SessionContext sessionContext;

   public Converter getConverter()
   {

      final BookingBean ejbProxy = this.sessionContext.getBusinessObject(BookingBean.class);

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

            return String.valueOf(((Booking) value).getId());
         }
      };
   }

   /*
    * Support adding children to bidirectional, one-to-many tables
    */

   private Booking add = new Booking();

   public Booking getAdd()
   {
      return this.add;
   }

   public Booking getAdded()
   {
      Booking added = this.add;
      this.add = new Booking();
      return added;
   }
}