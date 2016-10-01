<?php
namespace me\fru1t\research\template\partial;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;

class Downloads extends Template {

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
  <div class="section-title">Downloads</div>
  <div>At a tad over 30GB of scraped data, we couldn't simply dump it into plain text files.
    We chose to take the MySQL route, utilizing the power of relational databases to store and
    manipulate our scrapes. Don't worry though, you'll only be downloading the interesting bits
    (ie. the relevant data, without the padding HTML) which is about 850MBdasdfasdfasdfasdfasdfasdf asdf asd fasd fasdfasasd. All required software
    we use is free and open source. Please take a minute to thank their developer's time and hard
    work. They enable us do this type of research and share it for free!</div>

  <!-- Quick start guide -->
  <div class="section-subtitle">Setup Guide:</div>
  <p>Here we'll walk you through getting up and running as quick as possible.</p>
  <p class="top-padded"><label for="quick-start-guide">Click here</label> to expand this guide.</p>
  <input type="checkbox" class="state" id="quick-start-guide" />
  <div class="flex guide">
    <div class="left-flex">
      <div class="title">This guide walks through downloading and running queries against the data
          set.</div>
      <ol class="step-by-step">
        <li>
          <p>Select the "Data + Server" download option below. Feel free to move onto the next steps
            while the package is downloading.</p>
          <p class="top-padded"><span class="bold">Fair warning:</span> the performance of the
            server correlates directory with the type of hard drive you run on your system. It's
            advisable to place and run the downloaded package on a solid state drive if possible,
            as it will vastly outperform a hard disk drive in terms of read speed. This translates
            into faster queries and data manipulation by orders of magnitude.</p>
        </li>
        <li>Download and install 64-bit MySQL Workbench. See "Required Software" for a link
          to their website.</li>
        <li>At this point, we require the data and server package, so wait until it's completely
          downloaded before continuing.</li>
        <li>Extract the contents of the downloaded package. Keep in mind that the extraction
          location is the location the server will be run from (ie. the hard drive that the data
          will be served from).</li>
        <li>Start the server by running the "start-server" batch file, and verify that the console
          window displays "ready for connections" as one of the last outputs. Feel free to minimize
          this console window, but do not close it!</li>
        <li>Start MySQL Workbench. We will now be working exclusively in this program, so unless
          specified otherwise, perform the remaining actions within the MySQL Workbench window.</li>
        <li>
          <p>Configure the workbench</p>
          <ul>
            <li>Edit > Preferences</li>
            <li>On the left, select "SQL Editor" (the text, not the arrow drop down)</li>
            <li>Under "MySQL Session", set both "keep-alive" and "read time out" to 60000
              (seconds). Increase the "Connection time out" to 6000 (seconds).</li>
            <li>Click "OK"</li>
          </ul>
        </li>
        <li>
          <p>Save a connection to the server</p>
          <ul>
            <li>In MySQL workbench, near the top, click the "+" symbol, next to "MySQL
              Connections". It should open a "Setup new Connection" dialogue box like the one
              to the right.</li>
            <li>
              <p>Fill out the fields (if not already filled out) to the following:</p>
              <ul>
                <li>Connection Name: FanFiction.net Database</li>
                <li>Connection Method: Standard (TCP/IP)</li>
                <li>Hostname: 127.0.0.1</li>
                <li>Port: 3306</li>
                <li>Username: root</li>
                <li>(Leave default schema blank)</li>
              </ul>
            </li>
            <li>Press "Test Connection". A warning dialogue may appear stating
              "Incompatible/nonstandard server version or connection protocol". This is just
              complaining that we're using MariaDB, so it's safe to "Continue Anyway". Verify that
              the "Successfully made the MySQL connection" popup appears. If a dialogue box appears
              asking for a password, just click "ok". There is no password, and thus, should
              just be blank.</li>
            <li>Click "OK" on the "Setup New Connection" pane. The connection should now appear
              in the main window of the workbench.</li>
          </ul>
        </li>
        <li>Open the connection to the server by double
          clicking on the <span class="bold">left half</span> of the "fanfiction.net database"
          button. If you get a connection warning about "Incompatable/nonstandard server
          version", click "Continue anyway". A new tab should open with an empty query
          tab.</li>
        <li>That's it! From here, you're ready to start running queries and exploring the
          database. Please see <a href="#using-the-data">Using the Data</a> for example queries
          and how to use MySQL Workbench.</li>
      </ol>
    </div>
    
    <!-- flex right -->
    <div class="flex-liquid">
      <img src="/img3.png" alt="img2" />
    </div>
  </div>

  <!-- Download Links -->
  <div class="section-subtitle">Download Options:</div>
  <p class="top-padded"><a>Download options coming soon.</a></p>
  
  <!-- Required software -->
  <div class="section-subtitle">Required Software:</div>
  <p class="padded">All of these software bundles are free and come prebuilt for Windows or Linux.
    OS X binaries can be built with a little more effort, but that process will not be covered
    here. A step by step guide is available for each program that covers installation and
    setup.</p>
    
  <!-- mysql workbench -->
  <div class="subsection"><a href="http://dev.mysql.com/downloads/workbench/">MySQL
    Workbench</a></div>
  <p>This software package allows us to more easily interact with the server. It provides a
    GUI for us to use, instead of having to type in commands in a terminal all day. Be
    sure to install the <span class="bold">64-bit</span> version.</p>
  <p>Installation is fairly straightforward (follow default options when prompted).</p>
    
  <!-- Maria DB -->
  <div class="subsection"><a href="https://mariadb.org/" target="_blank">MariaDB Server</a> OR
      <a href="https://www.mysql.com/" target="_blank"> MySQL Server</a></div>
  <p><span class="bold">Note: </span>Installing this software is only required if you choose the
    "Data Only" download option. The "Data + Server" option contains a self packaged version
    of MariaDB included.</p>
  <p>For those installing their own copy, please be sure to choose the
    <span class="bold">64-bit</span> version. The 32-bit versions cannot handle the size of
    table we will be throwing at it. We prefer MariaDB server over MySQL server, but for
    <a href="http://programmers.stackexchange.com/questions/120178/whats-the-difference-between-mariadb-and-mysql"
       target="_blank">negligible reasons</a>. Feel free to download the latest (stable)
    version of either. Note that, we might use the term "MySQL server" which just refers to
    the server you have installed (MariaDB or MySQL).</p>
  <p><label for="software-maria-db">Click here</label> for a step-by-step installation
    guide.</p>
  <input type="checkbox" class="state" id="software-maria-db" />
  <div class="flex guide">
    <!-- flex left -->
    <div class="left-flex">
      <!-- Step by Step -->
      <p class="title">This guide will help you download and setup the latest version of
        the MariaDB server and import the data set onto it.</p>
      <ol class="step-by-step">
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
          window rather than as a system service. Be careful with batch scripts. Please feel
          free to right click and "edit" the script to see the contents before executing.</li>
        <li>Run the script by double clicking on it. Verify that the line "bin\mysqld: ready
          for connections" appears in the console as one of the last lines. If it doesn't,
          be sure that you've placed the script in the database root folder (the one in the
          image to the right).</li>
        <li>Install MySQL Workbench. See "Required Software" for a download link.</li>
        <li>From the Workbench, open a new connection to the MySQL server. See steps 6 and
          beyond in the "Setup Guide" for a walkthrough on how to do this.</li>
        <li>On the left side, click on "Data Import/Restore" under "Management". If this label
          isn't available, you may need to click on the "management" tab at the bottom of the
          navigator.</li>
        <li>
          In the Data Import window that appears, fill out the following information as follows
          (if a setting doesn't appear here, it's safe to leave as default):
          <ul>
            <li>Select "Import from Self-Contained File"</li>
            <li>Click the address bar to the right of "Import from Self-Contained File"
              and point it towards the downloaded data.</li>
            <li>Near the bottom, verify the drop down is selected on "Dump Structure
              and Data"</li>
          </ul>
        </li>
        <li>
          <p>Click "Start Import". This may take some time depending on what hardware the server
            is running on (eg. a solid state drive will import in about 10 minutes whereas a
            hard disk drive takes an upwards of an hour or more).</p>
          <p class="top-padded"><span class="bold">Note:</span> If MySQL Workbench disconnects
            due to timeout or crashes AFTER the import has started, don't worry. MySQL
            server is  still importing data. If, however, the workbench crashes, it
            will no longer notify you of a finished import. Instead, you need to monitor
            the task manager resources. Specifically, drive usage. Importing will use
            60-100% of drive usage. You will know when it's done when the usage drops back
            to "normal" (~0%).</p>
        </li>
        <li>That's it! We've set up MariaDB with our data.</li>
      </ol>
    </div>
    
    <!-- Flex right -->
    <div class="flex-liquid">
      <img src="/img2.png" alt="img2" />
      <img src="/img4.png" alt="img2" />
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
