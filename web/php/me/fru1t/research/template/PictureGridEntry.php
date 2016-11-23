<?php
namespace me\fru1t\research\template;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;

/**
 *
 */
class PictureGridEntry extends Template {
  const FIELD_URL = "url";

  /**
   * Produces the content this template defines in the form of an HTML string. This method is passed
   * a map with template field names as keys, and values that the content page provides.
   *
   * @param string[] $fields An associative array mapping fields to ContentField objects.
   * @return string
   */
  public static function getTemplateRenderContents_internal(array $fields): string {
    return <<<HTML
  <a href="{$fields[self::FIELD_URL]}" target="_blank">
    <img src="{$fields[self::FIELD_URL]}" alt="img" />
  </a>";
HTML;
  }

  /**
   * Provides the fields this template contains.
   *
   * @return TemplateField[]
   */
  static function getTemplateFields_internal(): array {
    return TemplateField::createFrom(self::FIELD_URL);
  }
}
