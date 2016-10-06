<?php
namespace me\fru1t\research\template\partial;
use me\fru1t\common\template\Template;
use me\fru1t\common\template\TemplateField;
use me\fru1t\research\template\AboutTable;
use me\fru1t\research\template\AboutTableColumn;
use me\fru1t\research\UsingTheDataTable;

class UsingTheData extends Template {

  /**
   * Produces the content this template defines in the form of an HTML string. This method is passed
   * a map with template field names as keys, and values that the content page provides.
   *
   * @param string[] $fields An associative array mapping fields to ContentField objects.
   * @return string
   */
  public static function getTemplateRenderContents_internal(array $fields): string {
    /** @var UsingTheDataTable[] $tables */
    $tables = [
        new UsingTheDataTable('story',
            'Holds all stories and their single-value metrics (eg. word count, but not genres).',
            [
                'id' => 'A unique, indexed identifier.',
                'fandom_id' => 'A foreign key to fandom (ie. the fandom the story belongs to).',
                'user_id' => 'A foreign key to user representing the author.',
                'rating_id' =>
                    'A foreign key to rating correlating to the story\'s content rating.',
                'language_id' => 'A foreign key to language.',
                'ff_story_id' => 'The unique ID fanfiction.net assigned to the story. Used to '
                    . 're-create the URL that links this story.',
                'title' => 'The title of the story.',
                'chapters' => 'Number of chapters, reported by Fanfiction.net.',
                'words' => 'The number of reports the story contains reported by Fanfiction.net.',
                'reviews' => 'Number of reviews, reported by Fanfiction.net.',
                'favorites' => 'Number of favorites, reported by Fanfiction.net.',
                'followers' => 'Number of followers, reported by Fanfiction.net.',
                'date_published' => 'The unix datetime stamp (in seconds) the story was published,'
                    . 'reported by Fanfiction.net',
                'date_updated' => 'The latest unix datetime stamp (in seconds) the story was '
                    . 'updated, or "-1" if not applicable, reported by Fanfiction.net.',
                'is_complete' => 'If the story is "complete" as reported by the author.'
            ]),
        new UsingTheDataTable('fandom',
            'Fandom or "source material". For example, Harry Potter, Bleach, etc.',
            [
                'id' => 'A unique, indexed identifier',
                'category_id' => 'A foreign key to the category the fandom belongs in.',
                'name' => 'The title of the fandom.',
                'url' => 'The absolute URL path to the fandom.'
            ]),
        new UsingTheDataTable('user',
            'Users on Fanfiction.net. In our database, this only represents the set of publishing '
                . 'authors as we only collected story information.',
            [
                'id' => 'A unique, indexed identifier',
                'ff_id' => 'The unique ID fanfiction.net assigned to the user.',
                'name' => 'The username set by the user. Not the user\'s real name.'
            ]),
        new UsingTheDataTable('category',
            'A mapping of category names to ids. For example, Book, Anime, etc.',
            [
                'id' => 'A unique, indexed identifier',
                'name' => 'The string representation of the category.'
            ]),
        new UsingTheDataTable('character',
            'A mapping of character names to ids. For example, Naruto, Spongebob Squarepants, etc.',
            [
                'id' => 'A unique, indexed identifier',
                'name' => 'The string representation of the character.'
            ]),
        new UsingTheDataTable('genre',
            'A mapping of genre names to ids. For example, Adventure, Hurt/Comfort, etc.',
            [
                'id' => 'A unique, indexed identifier',
                'name' => 'The string representation of the genre.'
            ]),
        new UsingTheDataTable('language',
            'A mapping of languages to ids. For example, English, Spanish, etc.',
            [
                'id' => 'A unique, indexed identifier',
                'name' => 'The string representation of the language.'
            ]),
        new UsingTheDataTable('rating',
            'A mapping of content rating names to ids. For example, T, M, etc',
            [
                'id' => 'A unique, indexed identifier',
                'name' => 'The string representation of the content rating.'
            ]),
        new UsingTheDataTable('story_character',
            'Maps characters to stories due to the many-to-many between the two.',
            [
                'story_id' => 'Foreign key representing the story.',
                'character_id' => 'Foreign key representing the character.'
            ]),
        new UsingTheDataTable('story_genre',
            'Maps genres to stories due to the many-to-many between the two.',
            [
                'story_id' => 'Foreign key representing the story.',
                'genre_id' => 'Foreign key representing the genre.'
            ])
    ];
    $tablesHtml = '';
    foreach ($tables as $table) {
      $columnsHtml = '';
      foreach ($table->columns as $name => $descr) {
        $columnsHtml .= AboutTableColumn::start()
            ->with(AboutTableColumn::FIELD_NAME, $name)
            ->with(AboutTableColumn::FIELD_DESCR, $descr)
            ->render(false, true);
      }
      $tablesHtml .= AboutTable::start()
          ->with(AboutTable::FIELD_NAME, $table->title)
          ->with(AboutTable::FIELD_DESCR, $table->descr)
          ->with(AboutTable::FIELD_COLUMNS, $columnsHtml)
          ->render(false, true);
    }

    return <<<HTML
<div class="container accessing-data-container">
  <div class="section-title">Using the Data</div>
  <!-- Terminology -->
  <div class="section-subtitle">Terminology:</div>
  <ul class="terminology-list">
    <li><span class="word">Category</span> is the medium with which the fandom is presented in.
      There are a total of 9 categories: Anime, Book, Cartoon, Comic, Game, Misc, Movie, Play,
      and TV. Categories are a creation of fanfiction.net.</li>
    <li><span class="word">Fandom</span> or <span class="word">source material</span> is the
      original, official anime/book/cartoon/etc. For example "Harry Potter" and "Bleach" are both
      fandoms. A fandom will always fall under a category.</li>
    <li><span class="word">Story</span> means a user written piece of work. Stories are always
      text-based due to the format of fanfiction.net, though the fandom it's associated to may
      or may not be.</li>
  </ul>
  
  <!-- Database tables -->
  <div class="section-subtitle">Database Tables:</div>
  <div class="about-tables">
    {$tablesHtml}
    <div class="clearfix"></div>
  </div>
  
  <!-- Using MySQL workbench -->
  <div class="section-subtitle">Using MySQL Workbench:</div>
  <p><label for="using-mysql-workbench">Click here</label> to expand the description image.</p>
  <input type="checkbox" class="state" id="using-mysql-workbench" />
  <div class="guide">
    <img src="/img5.png" alt="k" />
    <div class="title">
      Tips:
    </div>
    <ul class="step-by-step">
      <li>Set 'fanfiction' as the default schema so that you can omit the schema in the queries</li>
      <li>You can press ctrl + enter to execute scripts from within the active query panel</li>
      <li>The workbench will automatically syntax highlight and underline errors</li>
      <li>Click the arrow next to 'tables' to see a list of all tables in the fanfiction
        database</li>
    </ul>
  </div>
  
  <!-- Example Queries -->
  <div class="section-subtitle">Example Queries:</div>
  <ul class="example-queries">
    <li>
      <div>Show the use of genres</div>
      <pre>
SELECT
  genre.name AS genre,
  COUNT(story_genre.story_id) AS stories
FROM story_genre
INNER JOIN genre ON genre.id = story_genre.genre_id
GROUP BY genre.name
ORDER BY stories DESC
</pre>
    </li>
    <li>
      <div>
        Basic statistical analysis
        <p>Note: while possible to obtain median directly through MySQL, it'd be wise to use a 3rd
          party program to do it. The SQL solution is not a pretty one.</p>
      </div>
      <pre>
SELECT
    COUNT(id) AS stories,
    COUNT(DISTINCT user_id) AS authors,
    COUNT(DISTINCT language_id) AS languages,
    SUM(chapters) AS `total chapters`,
    AVG(chapters) AS `average chapters`,
    SUM(words) AS `total words`,
    AVG(words) AS `average words`,
    SUM(reviews) AS `total reviews`,
    AVG(reviews) AS `average reviews`,
    SUM(favorites) AS `total favorites`,
    AVG(favorites) AS `average favorites`,
    SUM(followers) AS `total followers`,
    AVG(followers) AS `average followers`
FROM story
</pre>
    </li>
    <li>
      <div>
        Basic statistical analysis by genre
        <p>Caution: This query may take > 30 minutes to execute.</p>
      </div>
      <pre>
SELECT
    genre.name AS genre,
    COUNT(story.id) AS stories,
    COUNT(DISTINCT story.user_id) AS authors,
    COUNT(DISTINCT story.language_id) AS languages,
    SUM(story.chapters) AS `total chapters`,
    AVG(story.chapters) AS `average chapters`,
    SUM(story.words) AS `total words`,
    AVG(story.words) AS `average words`,
    SUM(story.reviews) AS `total reviews`,
    AVG(story.reviews) AS `average reviews`,
    SUM(story.favorites) AS `total favorites`,
    AVG(story.favorites) AS `average favorites`,
    SUM(story.followers) AS `total followers`,
    AVG(story.followers) AS `average followers`
FROM story_genre
INNER JOIN story ON story.id = story_genre.story_id
INNER JOIN genre ON genre.id = story_genre.genre_id
GROUP BY genre
</pre>
    </li>
  </ul>
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
