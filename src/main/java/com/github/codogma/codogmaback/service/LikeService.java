package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

  private final ArticleRepository articleRepository;

  @Async("likeTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void incrementLikesCount(Long articleId) {
    articleRepository.incrementLikesCount(articleId);
  }

  @Async("likeTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void decrementLikesCount(Long articleId) {
    articleRepository.decrementLikesCount(articleId);
  }
}
