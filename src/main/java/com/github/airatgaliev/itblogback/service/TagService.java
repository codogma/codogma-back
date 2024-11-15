package com.github.airatgaliev.itblogback.service;

import com.github.airatgaliev.itblogback.dto.GetTag;
import com.github.airatgaliev.itblogback.model.TagModel;
import com.github.airatgaliev.itblogback.repository.TagRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

  private final TagRepository tagRepository;

  @Transactional
  public List<GetTag> getTagsByNameContaining(String name) {
    return tagRepository.findTop10ByNameStartingWithIgnoreCase(name).stream()
        .map(this::convertTagToDTO).toList();
  }

  private GetTag convertTagToDTO(TagModel tag) {
    return GetTag.builder().id(tag.getId()).name(tag.getName()).build();
  }
}
