<?php
namespace me\fru1t\research\template\partial;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;

/**
 *
 */
class OurSubmission extends Template {

  /**
   * Produces the content this template defines in the form of an HTML string. This method is passed
   * a map with template field names as keys, and values that the content page provides.
   *
   * @param string[] $fields An associative array mapping fields to ContentField objects.
   * @return string
   */
  public static function getTemplateRenderContents_internal(array $fields): string {
    return <<<HTML
<div class="container">
  <div class="section-title">Our Submission</div>
  <div>Where No One Has Gone Before: The First Metadata Set of the World's Largest Fanfiction
      Repository</div>
  <div class="flex submission-flex">
    <div class="flex-liquid submission-text">
      <div class="section-subtitle">Researchers:</div>
      <ul class="sub-researchers">
        <li><a href="https://www.linkedin.com/in/kodlee-yin-82ba0aa4"
               target="_blank">Kodlee Yin</a>,
            University of Washington, Information School, Undergraduate Student</li>
        <li><a href="http://faculty.washington.edu/aragon/"
               target="_blank">Cecilia Aragon</a>,
          University of Washington, Human Centered Design & Engineering, Professor</li>
        <li>Katie Davis,
          University of Washington, Information School, Assistant Professor</li>
        <li>Sarah Evans,
          University of Washington, School of Education, PhD Student</li>
      </ul>
      
      <div class="section-subtitle">Abstract:</div>
      <div>With its roots dating to popular television shows of the 1960s
        such as Star Trek, fanfiction has blossomed into an extremely popular form of creative
        expression. The transition from printed zines to online fanfiction repositories has
        facilitated this growth in popularity, with millions of fans writing stories and adding to
        sites such as Fanfiction.net each day. Enthusiasts are sharing their writing, reading
        stories written by others, and helping each other to grow as writers. Yet, this domain is
        often undervalued by society and understudied by researchers. Fanfiction.net, the world’s
        largest fanfiction repository, has never been systematically studied. The corpus is large
        and growing. In this paper, we present the first full release of all Fanfiction.net
        metadata. We use visual analytics techniques to draw several intriguing insights from the
        data and show the potential for future research. We have also made the open source
        database of raw metadata available so that other researchers can explore further
        questions related to online fanfiction communities.</div>
    </div>
    <div class="sub-download">
        <a><i class="fa fa-file-pdf-o"></i><br />Download link coming soon...</a>
    </div>
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
