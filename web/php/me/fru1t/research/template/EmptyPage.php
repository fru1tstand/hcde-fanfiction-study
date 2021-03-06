<?php
namespace me\fru1t\research\template;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;

/**
 * Defines an empty page template
 */
class EmptyPage extends Template {
  const FIELD_HTML_TITLE = "html-title";
  const FIELD_BODY = "body";

  /**
   * Produces the content this template defines in the form of an HTML string. This method is passed
   * a map with template field names as keys, and values that the content page provides.
   *
   * @param string[] $fields An associative array mapping fields to ContentField objects.
   * @return string
   */
  public static function getTemplateRenderContents_internal(array $fields): string {
    return <<<HTML
<!DOCTYPE html>
<html lang="en">
<head>
	<title>Fru1tMe Research - {$fields[self::FIELD_HTML_TITLE]}</title>
	<meta charset="UTF-8" />
	
	<link href="https://fonts.googleapis.com/css?family=Raleway" rel="stylesheet">
	<link type="text/css" rel="stylesheet" href="styles.css" />
	<link type="text/css" rel="stylesheet"
	      href="https://maxcdn.bootstrapcdn.com/font-awesome/4.6.3/css/font-awesome.min.css" />
</head>

<body>
	{$fields[self::FIELD_BODY]}
	
<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-27866066-6', 'auto');
  ga('send', 'pageview');

</script>
</body>
</html>
HTML;
  }

  /**
   * Provides the fields this template contains.
   *
   * @return TemplateField[]
   */
  static function getTemplateFields_internal(): array {
    return [
        TemplateField::newBuilder()->called(self::FIELD_HTML_TITLE)->asRequired()->build(),
        TemplateField::newBuilder()->called(self::FIELD_BODY)->asRequired()->build()
    ];
  }
}
