package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.CreateArticleImageDTO;
import com.github.codogma.codogmaback.dto.PaletteDTO;
import com.github.codogma.codogmaback.dto.SwatchDTO;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.model.ArticleImageModel;
import com.github.codogma.codogmaback.model.ArticleModel;
import com.github.codogma.codogmaback.model.Palette;
import com.github.codogma.codogmaback.model.Swatch;
import com.github.codogma.codogmaback.repository.ArticleImageRepository;
import com.github.codogma.codogmaback.repository.ArticleRepository;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

  private final ArticleRepository articleRepository;
  private final ArticleImageRepository articleImageRepository;
  private final ExceptionFactory exceptionFactory;
  private final FileUploadUtil fileUploadUtil;

  @Transactional
  public String uploadArticleImage(Long articleId, CreateArticleImageDTO createArticleImage) {
    ArticleModel articleModel = articleRepository.findById(articleId)
        .orElseThrow(() -> exceptionFactory.articleNotFound(articleId));

    String urlPath = fileUploadUtil.uploadArticleImage(createArticleImage.getImage());

    PaletteDTO paletteDTO = createArticleImage.getPalette();

    Palette palette = mapPaletteDTO(paletteDTO);

    articleImageRepository.findByArticleIdAndIsPreviewIsTrue(articleId)
        .ifPresentOrElse(articleImage -> {
          articleImage.setPreview(createArticleImage.isPreview());
          articleImage.setImageUrl(urlPath);
          articleImage.setPalette(palette);
        }, () -> {
          ArticleImageModel articleImage = ArticleImageModel.builder().article(articleModel)
              .isPreview(createArticleImage.isPreview()).palette(palette).imageUrl(urlPath).build();
          articleModel.getImages().add(articleImage);
        });

    return urlPath;
  }

  private Palette mapPaletteDTO(PaletteDTO dto) {
    return Palette.builder().vibrant(mapSwatch(dto.getVibrant())).muted(mapSwatch(dto.getMuted()))
        .darkVibrant(mapSwatch(dto.getDarkVibrant())).darkMuted(mapSwatch(dto.getDarkMuted()))
        .lightVibrant(mapSwatch(dto.getLightVibrant())).lightMuted(mapSwatch(dto.getLightMuted()))
        .build();
  }

  private Swatch mapSwatch(SwatchDTO swatchDTO) {
    if (swatchDTO == null) {
      return null;
    }
    return Swatch.builder().r(swatchDTO.getR()).g(swatchDTO.getG()).b(swatchDTO.getB())
        .h(swatchDTO.getH()).s(swatchDTO.getS()).l(swatchDTO.getL()).hex(swatchDTO.getHex())
        .titleTextColor(swatchDTO.getTitleTextColor()).bodyTextColor(swatchDTO.getBodyTextColor())
        .build();
  }
}