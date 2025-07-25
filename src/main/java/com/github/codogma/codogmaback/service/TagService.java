package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

  private final TagRepository tagRepository;

  @Transactional
  @Cacheable(value = "tagsByName", key = "#name")
  public List<GetTag> getTagsByNameContaining(String name) {
    return tagRepository.findTop10ByNameStartingWithIgnoreCase(name).stream()
        .map(this::convertTagToDTO).toList();
  }

  private GetTag convertTagToDTO(TagModel tag) {
    return GetTag.builder().id(tag.getId()).name(tag.getName()).build();
  }
}
