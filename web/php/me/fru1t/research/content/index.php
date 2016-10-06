<?php
namespace me\fru1t\research\content;
use me\fru1t\research\template\EmptyPage;
use me\fru1t\research\template\partial\Downloads;
use me\fru1t\research\template\partial\OurSubmission;
use me\fru1t\research\template\partial\UsingTheData;

$ourSubmission = OurSubmission::start()->render(false, true);
$downloads = Downloads::start()->render(false, true);
$usingTheData = UsingTheData::start()->render(false, true);

$body = <<<HTML
<div class="content-push"></div>
<div class="container">
  <div class="page-title">Chi 2017 Note - FanFiction.net Metadata</div>
  <ul class="page-nav">
    <li><a href="#our-submission">Our Submission</a></li>
    <li class="spacer"></li>
    <li><a href="#downloads">Downloads &amp; Setting Up</a></li>
    <li class="spacer"></li>
    <li><a href="#using-the-data">Using the Data</a></li>
  </ul>
</div>

<!-- Carousel of imagesss :D -->
<div class="content-push"></div>
<div class="carousel">
  <div class="padding"></div>
  
  <img src="/img1.png" alt="img1" />
  <img src="/img1.png" alt="img1" />
  <img src="/img1.png" alt="img1" />
  <img src="/img1.png" alt="img1" />
  
  <div class="padding"></div>
</div>

<!-- Our Submission -->
<div class="content-push" id="our-submission"></div>
{$ourSubmission}

<!-- Downloads/Setup -->
<div class="content-push" id="downloads"></div>
{$downloads}

<!-- Using the Data -->
<div class="content-push" id="using-the-data"></div>
{$usingTheData}

<div class="content-push"></div>
HTML;

EmptyPage::start()
    ->with(EmptyPage::FIELD_HTML_TITLE, "A title")
    ->with(EmptyPage::FIELD_BODY, $body)
    ->render();
