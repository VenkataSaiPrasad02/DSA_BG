package com.dsabg.problem.specification;

import org.springframework.data.jpa.domain.Specification;
import com.dsabg.problem.entity.Problem;

import java.util.List;

public class ProblemSpecification {
    public static Specification<Problem> search(String search){
        return (root,query,criteriaBuilder)->{
            if(search==null||search.isBlank()){
                return criteriaBuilder.conjunction();
            }
            String keyword="%"+search.toLowerCase()+"%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),keyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("slug")),keyword
                    )
            );
        };
    }
    public static Specification<Problem> published() {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        Problem.ProblemStatus.PUBLISHED
                );
    }
    public static Specification<Problem> difficulty(Problem.Difficulty difficulty) {

        return (root, query, cb) -> {

            if (difficulty == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("difficulty"), difficulty);
        };
    }
    public static Specification<Problem> categories(List<String> categories) {

        return (root, query, cb) -> {

            if (categories == null || categories.isEmpty()) {
                return cb.conjunction();
            }

            query.distinct(true);

            return root.join("categories")
                    .get("name")
                    .in(categories);
        };
    }
}
