package com.database.criteria;

import com.database.model.DominiumOffer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

/**
 * Created by miras108 on 2017-01-07.
 */
public class DominiumOfferCriteria {
    private SessionFactory sessionFactory;

    public DominiumOfferCriteria(SessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
    }

    public DominiumOffer getByUrl(String url) {
        Session session = sessionFactory.openSession();
        Criteria criteria = session.createCriteria(DominiumOffer.class)
                .add(Restrictions.eq("url", url));

        return (DominiumOffer) criteria.uniqueResult();
    }
}
