<?php
namespace me\fru1t\research\template;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;

/**
 *
 */
class AboutTable extends Template {
  const FIELD_NAME = "name";
  const FIELD_DESCR = "descr";
  const FIELD_COLUMNS = "columns";

  /**
   * Produces the content this template defines in the form of an HTML string. This method is passed
   * a map with template field names as keys, and values that the content page provides.
   *
   * @param string[] $fields An associative array mapping fields to ContentField objects.
   * @return string
   */
  public static function getTemplateRenderContents_internal(array $fields): string {
    return <<<HTML
<div class="table">
  <div class="title">{$fields[self::FIELD_NAME]}</div>
  <div class="descr">{$fields[self::FIELD_DESCR]}</div>
  <div class="column-container">
    {$fields[self::FIELD_COLUMNS]}
  </div>
</div>
HTML;

  }

  /**
   * Provides the fields this template contains.
   *
   * @return TemplateField[]
   */
  static function getTemplateFields_internal(): array {
    return TemplateField::createFrom(self::FIELD_NAME, self::FIELD_COLUMNS, self::FIELD_DESCR);
  }
}