<?php
namespace me\fru1t\research\template;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;

/**
 *
 */
class AboutTableColumn extends Template {
  const FIELD_NAME = "name";
  const FIELD_DESCR = "descr";

  /**
   * Produces the content this template defines in the form of an HTML string. This method is passed
   * a map with template field names as keys, and values that the content page provides.
   *
   * @param string[] $fields An associative array mapping fields to ContentField objects.
   * @return string
   */
  public static function getTemplateRenderContents_internal(array $fields): string {
    return <<<HTML
<div class="column">
  <span class="name">{$fields[self::FIELD_NAME]}</span>
  <span class="descr">{$fields[self::FIELD_DESCR]}</span>
</div>
HTML;

  }

  /**
   * Provides the fields this template contains.
   *
   * @return TemplateField[]
   */
  static function getTemplateFields_internal(): array {
    return TemplateField::createFrom(self::FIELD_NAME, self::FIELD_DESCR);
  }
}
