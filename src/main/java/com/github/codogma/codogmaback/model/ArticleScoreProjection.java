package com.github.codogma.codogmaback.model;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ProjectionConstructor;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ScoreProjection;

@ProjectionConstructor
public record ArticleScoreProjection(@ScoreProjection float score, ArticleModel articleModel) {

}
