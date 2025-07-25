package com.github.codogma.codogmaback.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

public class ContentUtil {

  private ContentUtil() {
  }

  public static String createHtmlPreview(String htmlContent, int maxLength) {
    Document document = Jsoup.parseBodyFragment(htmlContent);
    Element body = document.body();

    int[] currentLength = {0};
    boolean[] textTruncated = {false};

    truncateElement(body, maxLength, currentLength, textTruncated);

    if (textTruncated[0]) {
      addEllipsisToEnd(body);
    }

    return body.html();
  }

  private static void truncateElement(Element element, int maxLength, int[] currentLength,
      boolean[] textTruncated) {
    Element nextSibling = null;
    for (Element child : element.children()) {
      if (textTruncated[0]) {
        child.remove();
      } else if (isHeadingTag(child)) {
        if (currentLength[0] < maxLength) {
          nextSibling = child.nextElementSibling();
          child.remove();
        } else {
          child.remove();
        }
      } else {
        truncateElement(child, maxLength, currentLength, textTruncated);
      }
    }

    for (TextNode textNode : element.textNodes()) {
      if (textTruncated[0]) {
        textNode.remove();
      } else {
        String text = textNode.text();
        int remainingLength = maxLength - currentLength[0];
        if (text.length() + currentLength[0] > maxLength) {
          String truncatedText = truncateTextAtSentenceBoundary(text, remainingLength);
          textNode.text(truncatedText);
          currentLength[0] = maxLength;
          textTruncated[0] = true;
          return;
        } else {
          currentLength[0] += text.length();
        }
      }
    }

    if (textTruncated[0] && nextSibling != null) {
      nextSibling.remove();
    }
  }

  private static void addEllipsisToEnd(Element element) {
    TextNode lastTextNode = findLastTextNode(element);
    if (lastTextNode != null) {
      String existingText = lastTextNode.text();
      if (!existingText.endsWith("...")) {
        lastTextNode.text(existingText + "...");
      }
    }
  }

  private static TextNode findLastTextNode(Element element) {
    TextNode lastTextNode = null;
    for (Element child : element.children()) {
      TextNode childLastTextNode = findLastTextNode(child);
      if (childLastTextNode != null) {
        lastTextNode = childLastTextNode;
      }
    }

    if (!element.textNodes().isEmpty()) {
      lastTextNode = element.textNodes().getLast();
    }

    return lastTextNode;
  }

  private static boolean isHeadingTag(Element element) {
    String tagName = element.tagName();
    return tagName.equals("h1") || tagName.equals("h2") || tagName.equals("h3") || tagName.equals(
        "h4") || tagName.equals("h5") || tagName.equals("h6");
  }

  private static String truncateTextAtSentenceBoundary(String text, int maxLength) {
    if (maxLength >= text.length()) {
      return text;
    }

    String truncated = text.substring(0, maxLength);
    int lastSentenceBoundary = findLastSentenceBoundary(truncated);

    if (lastSentenceBoundary != -1) {
      return text.substring(0, lastSentenceBoundary).trim();
    }

    return truncated.trim();
  }

  private static int findLastSentenceBoundary(String text) {
    int lastPeriod = text.lastIndexOf('.');
    int lastExclamation = text.lastIndexOf('!');
    int lastQuestion = text.lastIndexOf('?');
    int maxIndex = Math.max(lastPeriod, Math.max(lastExclamation, lastQuestion));
    return maxIndex + 1;
  }
}