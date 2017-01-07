package com.database.dao;

import com.database.criteria.DominiumOfferCriteria;
import com.database.model.DominiumOffer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by miras108 on 2017-01-07.
 */
public class DominiumOfferDao {
    private SessionFactory sessionFactory;
    private DominiumOfferCriteria dominiumOfferCriteria;

    public DominiumOfferDao(SessionFactory sessionFactory, DominiumOfferCriteria dominiumOfferCriteria) {
        this.sessionFactory = sessionFactory;
        this.dominiumOfferCriteria = dominiumOfferCriteria;
    }

    public DominiumOffer getDominiumOfferByUrl(String url) {
        return dominiumOfferCriteria.getByUrl(url);
    }

    @Transactional
    public void save(DominiumOffer dominiumOffer) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();
        session.saveOrUpdate(dominiumOffer);
        transaction.commit();
    }

}
