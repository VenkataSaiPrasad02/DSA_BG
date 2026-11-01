package com.dsabg.problem.service.impl;

import com.dsabg.problem.dto.request.PatternRequest;
import com.dsabg.problem.entity.Category;
import com.dsabg.problem.entity.Pattern;
import com.dsabg.problem.repository.CategoryRepository;
import com.dsabg.problem.repository.PatternRepository;
import com.dsabg.problem.service.PatternService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PatternServiceImpl implements PatternService {

    private final PatternRepository patternRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Pattern createPattern(PatternRequest patternRequest) {

        if (patternRepository.existsBySlug(patternRequest.slug())) {
            throw new RuntimeException("Pattern slug already exists");
        }

        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(patternRequest.categoryIds())
        );

        if (categories.size() != patternRequest.categoryIds().size()) {
            throw new RuntimeException("One or more category IDs are invalid");
        }

        Pattern pattern = new Pattern();
        pattern.setName(patternRequest.name());
        pattern.setSlug(patternRequest.slug());
        pattern.setDescription(patternRequest.description());
        pattern.setCategories(categories);

        return patternRepository.save(pattern);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pattern> getPatternsByCategoryIds(List<Long> categoryIds) {

        return patternRepository
                .findDistinctByCategories_CategoryIdIn(categoryIds);
    }

    @Override
    @Transactional
    public void assignCategories(Long patternId, Set<Long> categoryIds) {

        Pattern pattern = patternRepository.findById(patternId)
                .orElseThrow(() ->
                        new RuntimeException("Pattern not found"));

        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(categoryIds)
        );

        if (categories.size() != categoryIds.size()) {
            throw new RuntimeException("One or more category IDs are invalid");
        }

        pattern.getCategories().addAll(categories);

        patternRepository.save(pattern);
    }
}