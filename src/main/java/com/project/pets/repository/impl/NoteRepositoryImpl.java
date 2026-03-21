package com.project.pets.repository.impl;

import com.project.pets.domain.Note;
import com.project.pets.domain.dto.DatatablesCriterias;
import com.project.pets.domain.dto.NoteSearchDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class NoteRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Note> findByDatatables(NoteSearchDto searchDto, DatatablesCriterias criterias) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Note> query = cb.createQuery(Note.class);
        Root<Note> note = query.from(Note.class);

        // Fetch Dog para evitar lazy loading
        note.fetch("dog", JoinType.LEFT);

        // Aplicar filtros
        List<Predicate> predicates = buildPredicates(cb, note, searchDto, criterias);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Aplicar ordenamiento
        applyOrdering(cb, query, note, criterias);

        TypedQuery<Note> typedQuery = entityManager.createQuery(query);

        // Aplicar paginación
        if (criterias.getStart() >= 0) {
            typedQuery.setFirstResult(criterias.getStart());
        }
        if (criterias.getLength() > 0) {
            typedQuery.setMaxResults(criterias.getLength());
        }

        return typedQuery.getResultList();
    }

    public Long countNotesByCriterias(NoteSearchDto searchDto, DatatablesCriterias criterias) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Note> note = query.from(Note.class);

        query.select(cb.count(note));

        // Aplicar los mismos filtros
        List<Predicate> predicates = buildPredicates(cb, note, searchDto, criterias);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    public Long countAllNotes() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Note> note = query.from(Note.class);

        query.select(cb.count(note));

        return entityManager.createQuery(query).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Note> note,
                                           NoteSearchDto searchDto, DatatablesCriterias criterias) {
        List<Predicate> predicates = new ArrayList<>();

        // Filtro por dogId si se proporciona
        if (searchDto.getDogId() != null) {
            predicates.add(cb.equal(note.get("dog").get("id"), searchDto.getDogId()));
        }

        // Búsqueda global (searchValue de DataTables)
        String searchValue = criterias.getSearchValue();
        if (searchValue != null && !searchValue.trim().isEmpty()) {
            String pattern = "%" + searchValue.toLowerCase() + "%";
            Predicate subjectLike = cb.like(cb.lower(note.get("subject")), pattern);
            predicates.add(subjectLike);
        }

        // Búsqueda por columna específica (si DataTables lo envía)
        if (criterias.getColumns() != null) {
            for (DatatablesCriterias.ColumnDef column : criterias.getColumns()) {
                if (column.isSearchable() && column.getSearch() != null
                        && column.getSearch().getValue() != null
                        && !column.getSearch().getValue().trim().isEmpty()) {

                    String columnSearchValue = column.getSearch().getValue();
                    String pattern = "%" + columnSearchValue.toLowerCase() + "%";

                    if ("subject".equals(column.getData())) {
                        predicates.add(cb.like(cb.lower(note.get("subject")), pattern));
                    } else if ("noteDate".equals(column.getData())) {
                        // Búsqueda por fecha (opcional, según tu necesidad)
                        // predicates.add(...);
                    }
                }
            }
        }

        return predicates;
    }

    private void applyOrdering(CriteriaBuilder cb, CriteriaQuery<Note> query,
                              Root<Note> note, DatatablesCriterias criterias) {
        if (criterias.getOrder() != null && !criterias.getOrder().isEmpty()) {
            DatatablesCriterias.ColumnOrder orderColumn = criterias.getOrder().get(0);
            int columnIndex = orderColumn.getColumn();
            String direction = orderColumn.getDir();

            if (criterias.getColumns() != null && columnIndex < criterias.getColumns().size()) {
                String columnName = criterias.getColumns().get(columnIndex).getData();

                Order order;
                if ("noteDate".equals(columnName)) {
                    order = "asc".equalsIgnoreCase(direction)
                            ? cb.asc(note.get("noteDate"))
                            : cb.desc(note.get("noteDate"));
                } else if ("subject".equals(columnName)) {
                    order = "asc".equalsIgnoreCase(direction)
                            ? cb.asc(note.get("subject"))
                            : cb.desc(note.get("subject"));
                } else {
                    // Por defecto, ordenar por noteDate descendente
                    order = cb.desc(note.get("noteDate"));
                }
                query.orderBy(order);
                return;
            }
        }

        // Orden por defecto
        query.orderBy(cb.desc(note.get("noteDate")));
    }
}

