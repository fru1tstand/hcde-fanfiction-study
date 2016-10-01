<?php
namespace me\fru1t\research\template\partial;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;

class UsingTheData extends Template {

  /**
   * Produces the content this template defines in the form of an HTML string. This method is passed
   * a map with template field names as keys, and values that the content page provides.
   *
   * @param string[] $fields An associative array mapping fields to ContentField objects.
   * @return string
   */
  public static function getTemplateRenderContents_internal(array $fields): string {
    return <<<HTML
<div class="container accessing-data-container">
  <div class="section-title">Using the Data</div>
  
  <!-- About the Database -->
  <div class="section-subtitle">Database Structure:</div>
  <p>The tables and their columns are explained in this section.</p>
  <div class="about-tables">
    
    <div class="table">asdfasdf</div>
    <div class="table">asdfasdf</div>
    <div class="table">asdfasdf</div>
    <div class="table">asdfasdf</div>
    <div class="table">asdfasdf</div>
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
    return [];
  }
}
