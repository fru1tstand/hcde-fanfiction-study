<?php
define("PHP_ROOT", $_SERVER['DOCUMENT_ROOT'] . "/../php");
require_once PHP_ROOT . "/me/fru1t/common/language/Autoload";
use me\fru1t\common\language\Autoload;
use me\fru1t\common\language\Session;
use me\fru1t\common\mysql\MySQL;
use me\fru1t\common\template\TemplateRouter;

// Autoload MUST be set up first.
Autoload::setup(PHP_ROOT);

// Optionally, other plugins
Session::setup("my-session-name");
MySQL::setup("a-host", "a username", "a password", "example database");

// Finally, our template router
TemplateRouter::openContentFileFromUrl("/../php/me/fru1t/example/", "index.php", "error.php");
