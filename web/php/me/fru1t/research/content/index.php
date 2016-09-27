<?php
namespace me\fru1t\research\content;
use me\fru1t\research\template\EmptyPage;

$body = <<<HTML
<div class="content-push"></div>
<div class="container">
  <div class="home-title">Chi 2017 Note - FanFiction.net Metadata</div>
  <ul class="home-nav">
    <li><a href="#our-submission">Our Submission</a></li>
    <li class="spacer"></li>
    <li><a href="#">Accessing the Data</a></li>
    <li class="spacer"></li>
    <li><a href="#">Related Works</a></li>
  </ul>
</div>

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
<div class="content-push"></div>
<div class="container">
  <div class="section-title" id="our-submission">Our Submission</div>
  <div class="flex submission-flex">
    <div class="flex-liquid submission-text">
      <div class="section-subtitle">Researchers:</div>
      <ul class="researchers">
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
      
      <div class="content-push-small"></div>
      <div class="section-subtitle">Abstract:</div>
      <p class="abstract-paragraph">With its roots dating to popular television shows of the 1960s
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
          questions related to online fanfiction communities.</p>
    </div>
    <div class="download">
        <a href="#"><i class="fa fa-file-pdf-o"></i><br />Download the PDF</a>
    </div>
  </div>
</div>

<!-- Accessing the Data -->
<div class="content-push"></div>
<div class="container accessing-data-container">
  <div class="section-title" id="access-data">Accessing the Data</div>
  <p>At a tad over 30GB of scraped data, we couldn't simply dump it into plain text files.
      We chose to take the MySQL route, utilizing the power of relational databases to store and
      manipulate our scrapes. Don't worry though, you'll only be downloading the interesting bits
      (ie. the relevant data, without the padding HTML) which is about 850MB. All required software
      we use is free and open source. Please take a minute to thank their developer's time and hard
      work. They enable us do this type of research and share it for free!</p>
  
  <div class="content-push-small"></div>
  <div class="section-subtitle">Required Software:</div>
  <p>All of these software bundles come prebuilt for Windows or Linux. OS X binaries can be built
      with a little more effort, but that process will not be covered here.</p>

  <ul class="required-software">
    <!-- Maria DB -->
    <li>
      <div><a href="https://mariadb.org/" target="_blank">MariaDB Server</a> OR
          <a href="https://www.mysql.com/" target="_blank"> MySQL Server</a></div>
      <input type="checkbox" class="state" id="software-maria-db" />
      <div class="flex software-setup">
        <!-- flex left -->
        <div class="required-software-flex">
          <p>Please be sure to choose the <span class="bold">64-bit</span> version. The 32-bit
              versions cannot handle the size of table we will be throwing at it. We also prefer
              MariaDB server over MySQL server, but for
              <a href="http://programmers.stackexchange.com/questions/120178/whats-the-difference-between-mariadb-and-mysql"
                 target="_blank">negligible reasons</a>. Feel free to download the latest (stable)
              version of either.</p>
          <p>Stuck/first time setting up? <label for="software-maria-db">Click here</label> to
              expand the step-by-step guide.</p>
          
          <!-- Step by Step -->
          <div class="hide">
            <p>This guide will help you download and setup the latest version of the MariaDB
                server.</p>
            <ol class="padded step-by-step">
              <li>Download the <span class="bold">64-bit ZIP file</span> (not the installer) from
                  the download page of the MariaDB website.</li>
              <li>Create a folder somewhere in your filesystem that will act as the data warehouse
                  for the database. We'll call this folder the database root folder.</li>
              <li>Extract the MariaDB zip file into the folder such that the database root folder
                  directly contains the folders named "bin", "data", etc, and the files named
                  "my-medium.ini", "README", etc. The database root folder should look something
                  like the image to the right.</li>
              <li>Download <a href="/start-server.bat">this script</a> and save it in the database
                  root folder as "start-server.bat". This script will run the database in a console
                  window rather than as a system service.</li>
              <li>Run the script by double clicking on it. Verify that the line "bin\mysqld: ready
                  for connections" appears in the console as one of the last lines. If it doesn't,
                  be sure that you've placed the script in the database root folder (the one in the
                  image to the right). Be careful with batch scripts. Please feel free to right
                  click and "edit" the script to see the contents before executing.</li>
              <li>Shut down the server by pressing "ctrl+c" in the console window. Verify that the
                  line "bin\mysql: Shutdown complete" appears in the console. You may now close the
                  console window.</li>
              <li>That's it!</li>
            </ol>
          </div>
        </div>
        
        <!-- Flex right -->
        <div class="flex-liquid"><img src="/img2.png" alt="img2" class="hide" /></div>
      </div>
    </li> <!-- /maria db -->
  
    <!-- mysql workbench -->
    <li>
      <div><a href="http://dev.mysql.com/downloads/workbench/">MySQL Workbench</a></div>
      <input type="checkbox" class="state" id="software-mysql-workbench" />
      <div class="flex software-setup">
        <!-- flex left -->
        <div class="required-software-flex">
          <p>This software package allows us to more easily interact with the server. It provides a
              GUI for us to use, instead of having to type in commands in a terminal all day. Be
              sure to install the <span class="bold">64-bit</span> version, and connect to the
              database.</p>
          <p><label for="software-mysql-workbench">Click here</label> for the step-by-step
              guide.</p>
          
          <!-- step by step -->
          <div class="hide">
            asdf
          </div>
        </div>
        
        <!-- flex right -->
        <div class="flex-liquid"><img src="/img2.png" alt="img2" class="hide" /></div>
      </div>
    </li> <!-- /mysql workbench -->
  
  </ul> 
</div>

<div class="content-push"></div>
HTML;

EmptyPage::start()
    ->with(EmptyPage::FIELD_HTML_TITLE, "A title")
    ->with(EmptyPage::FIELD_BODY, $body)
    ->render();
