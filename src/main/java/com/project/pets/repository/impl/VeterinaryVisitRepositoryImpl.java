package com.project.pets.repository.impl;

import com.project.pets.domain.VeterinaryVisit;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.domain.dto.veterinaryvisit.VeterinaryVisitSearchDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VeterinaryVisitRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public List<VeterinaryVisit> findByDatatables(VeterinaryVisitSearchDto searchDto, DatatablesCriterias criterias) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<VeterinaryVisit> query = cb.createQuery(VeterinaryVisit.class);
        Root<VeterinaryVisit> visit = query.from(VeterinaryVisit.class);

        visit.fetch("dog", JoinType.LEFT);
        visit.fetch("veterinary", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, visit, searchDto, criterias);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        applyOrdering(cb, query, visit, criterias);

        TypedQuery<VeterinaryVisit> typedQuery = entityManager.createQuery(query);

        if (criterias.getStart() >= 0) {
            typedQuery.setFirstResult(criterias.getStart());
        }
        if (criterias.getLength() > 0) {
            typedQuery.setMaxResults(criterias.getLength());
        }

        return typedQuery.getResultList();
    }

    public Long countByCriterias(VeterinaryVisitSearchDto searchDto, DatatablesCriterias criterias) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VeterinaryVisit> visit = query.from(VeterinaryVisit.class);

        query.select(cb.count(visit));

        List<Predicate> predicates = buildPredicates(cb, visit, searchDto, criterias);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    public Long countAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<VeterinaryVisit> visit = query.from(VeterinaryVisit.class);
        query.select(cb.count(visit));
        return entityManager.createQuery(query).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<VeterinaryVisit> visit,
                                            VeterinaryVisitSearchDto searchDto, DatatablesCriterias criterias) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchDto.getDogId() != null) {
            predicates.add(cb.equal(visit.get("dog").get("id"), searchDto.getDogId()));
        }

        String searchValue = criterias.getSearchValue();
        if (searchValue != null && !searchValue.trim().isEmpty()) {
            String pattern = "%" + searchValue.toLowerCase() + "%";
            Predicate reasonLike = cb.like(cb.lower(visit.get("reason")), pattern);
            Predicate dateLike = cb.like(
                    cb.function("TO_CHAR", String.class, visit.get("visitDate"), cb.literal("YYYY-MM-DD")),
                    pattern);
            predicates.add(cb.or(reasonLike, dateLike));
        }

        if (criterias.getColumns() != null) {
            for (DatatablesCriterias.ColumnDef column : criterias.getColumns()) {
                if (column.isSearchable() && column.getSearch() != null
                        && column.getSearch().getValue() != null
                        && !column.getSearch().getValue().trim().isEmpty()) {

                    String pattern = "%" + column.getSearch().getValue().toLowerCase() + "%";

                    if ("reason".equals(column.getData())) {
                        predicates.add(cb.like(cb.lower(visit.get("reason")), pattern));
                    } else if ("visitDate".equals(column.getData())) {
                        predicates.add(cb.like(
                                cb.function("TO_CHAR", String.class, visit.get("visitDate"), cb.literal("YYYY-MM-DD")),
                                pattern));
                    }
                }
            }
        }

        return predicates;
    }

    private void applyOrdering(CriteriaBuilder cb, CriteriaQuery<VeterinaryVisit> query,
                               Root<VeterinaryVisit> visit, DatatablesCriterias criterias) {
        if (criterias.getOrder() != null && !criterias.getOrder().isEmpty()) {
            DatatablesCriterias.ColumnOrder orderColumn = criterias.getOrder().get(0);
            int columnIndex = orderColumn.getColumn();
            String direction = orderColumn.getDir();

            if (criterias.getColumns() != null && columnIndex < criterias.getColumns().size()) {
                String columnName = criterias.getColumns().get(columnIndex).getData();

                Order order;
                if ("visitDate".equals(columnName)) {
                    order = "asc".equalsIgnoreCase(direction)
                            ? cb.asc(visit.get("visitDate"))
                            : cb.desc(visit.get("visitDate"));
                } else if ("reason".equals(columnName)) {
                    order = "asc".equalsIgnoreCase(direction)
                            ? cb.asc(visit.get("reason"))
                            : cb.desc(visit.get("reason"));
                } else {
                    order = cb.desc(visit.get("visitDate"));
                }
                query.orderBy(order);
                return;
            }
        }

        query.orderBy(cb.desc(visit.get("visitDate")));
    }
}

