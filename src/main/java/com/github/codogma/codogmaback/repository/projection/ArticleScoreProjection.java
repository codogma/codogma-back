package com.github.codogma.codogmaback.repository.projection;

import com.github.codogma.codogmaback.model.ArticleModel;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ProjectionConstructor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ScoreProjection;

@ProjectionConstructor
public record ArticleScoreProjection(@ScoreProjection float score, ArticleModel articleModel) {

}
