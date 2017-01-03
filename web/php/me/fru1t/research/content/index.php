<?php
namespace me\fru1t\research\content;
use me\fru1t\research\template\ColumnListEntry;
use me\fru1t\research\template\EmptyPage;
use me\fru1t\research\template\PictureGridEntry;

$columnsHtml = "";
$picsHtml = "";

$columns = array(
    "anonymized_user_id" => "A randomized string representing the creator of the work.",
    "anonymized_story_title" => "A randomized string representing the title of the work.",
    "fandom_name" => "The source material the entry is written on (eg. Naruto, Harry Potter, ...).",
    "fandom_category" => "The type of fandom the entry belongs to (eg. TV show, Book, Anime).",
    "year_month_published" => "The year and month of the work's publication.",
    "is_complete" => "If the author has denoted that the entry is complete or not (ie. work-in-progress).",
    "content_rating" => "The content rating for the entry (eg. K for Kids, T for Teens, and M for Mature).",
    "language" => "The language the story was written in.",
    "genres" => "Story genres (eg. Drama, Romance, Adventure, ...).",
    "chapters" => "The number of chapters in the entry.",
    "words" => "The number of words in the entry.",
    "reviews" => "The number of user reviews associated to the entry.",
    "favorites" => "The number of other users who've favorited the entry.",
    "followers" => "The number of other users who've followed the entry."
);
foreach ($columns as $columnName => $columnDescr) {
  $columnsHtml .= ColumnListEntry::start()
      ->with(ColumnListEntry::FIELD_NAME, $columnName)
      ->with(ColumnListEntry::FIELD_DESCR, $columnDescr)
      ->render(false, true);
}

$pics = array(
    "https://s3-us-west-1.amazonaws.com/fm-research/web/web_pic1.png",
    "https://s3-us-west-1.amazonaws.com/fm-research/web/web_pic2.png",
    "https://s3-us-west-1.amazonaws.com/fm-research/web/web_pic3.png",
    "https://s3-us-west-1.amazonaws.com/fm-research/web/web_pic4.png",
    "https://s3-us-west-1.amazonaws.com/fm-research/web/web_pic5.png",
    "https://s3-us-west-1.amazonaws.com/fm-research/web/web_pic6.png"
);
foreach ($pics as $pic) {
  $picsHtml .= PictureGridEntry::start()
      ->with(PictureGridEntry::FIELD_URL, $pic)
      ->render(false, true);
}

$body = <<<HTML
<div class="content-push"></div>
<div class="container">
  <div class="page-title">Chi Note 2017 - Fanfiction Metadata</div>
</div>

<!-- Pictures -->
<div class="content-push-small"></div>
<div class="picture-grid">
  $picsHtml
</div>

<!-- Submission -->
<div class="content-push"></div>
<div class="container">
  <div class="section-subtitle">Abstract:</div>
  <p>With its roots dating to popular television shows of the 1960s such as Star Trek, fanfiction
    has blossomed into an extremely popular form of creative expression. The transition from
    printed zines to online fanfiction repositories has facilitated this growth in popularity, with
    millions of fans writing stories and adding to sites such as FanFiction.net, Archive Of Our Own,
    and many others each day. Enthusiasts are sharing their writing, reading stories written by
    others, and helping each other to grow as writers. Yet, this domain is often undervalued by
    society and understudied by researchers. Fanfiction repositories in particular have rarely been
    quantitatively analyzed in their entirety. The corpora are large and growing. In this paper,
    we present the first full, anonymous release of the metadata from a large fanfiction repository.
    We use visual analytics techniques to draw several intriguing insights from the data and show
    the potential for future research. We have also made the open source database of metadata
    available, anonymized via algorithms that satisfy differential privacy, so that other
    researchers can explore further questions related to online fanfiction communities.</p>
</div>

<!-- What we offer -->
<div class="content-push"></div>
<div class="container">
  <div class="section-title">What we Provide</div>
  <p>All data entries are anonymized and will satisfy differential privacy constraints so that
    information cannot be traced back and associated to the individual that produced it. The
    information we're releasing contains only the metadata of the stories published on a large
    fanfiction repository. The data contains the following columns, given in order from left to
    right.</p>
  <div class="content-push-small"></div>
  <ul class="column-list">
    $columnsHtml
  </ul>
</div>

<!-- Download -->
<div class="content-push"></div>
<div class="container">
  <div class="section-title">Download</div>
  <div class="content-push-small"></div>
  <div class="download-container">
    <a href="https://s3-us-west-1.amazonaws.com/fm-research/stories-csv.zip"><i class="fa fa-file-text-o"></i><span>csv <span>(649 MB)</span></span></a>
    <a href="https://s3-us-west-1.amazonaws.com/fm-research/stories-xml.zip"><i class="fa fa-file-code-o"></i><span>xml <span>(723 MB)</span></span></a>
  </div>
</div>

<div class="content-push"></div>
HTML;

EmptyPage::start()
    ->with(EmptyPage::FIELD_HTML_TITLE, "Chi Note 2017 - Fanfiction Metadata")
    ->with(EmptyPage::FIELD_BODY, $body)
    ->render();
