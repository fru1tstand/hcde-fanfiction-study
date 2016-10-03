<?php
namespace me\fru1t\research;

/**
 *
 */
class UsingTheDataTable {
  public $title;
  public $descr;
  public $columns;

  public function __construct(string $title, string $descr, array $columns) {
    $this->title = $title;
    $this->descr = $descr;
    $this->columns = $columns;
  }
}
