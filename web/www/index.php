<?php
define("PHP_ROOT", $_SERVER['DOCUMENT_ROOT'] . '/../php');
require_once PHP_ROOT . '/me/fru1t/common/language/Autoload.php';
use me\fru1t\common\language\Autoload;
use me\fru1t\common\language\Http;
use me\fru1t\common\router\Route;
use me\fru1t\common\router\Router;
use me\fru1t\common\template\Templates;

Autoload::setup(PHP_ROOT);
Router::setup()
    ->setContentDirectory('../php/me/fru1t/research/content')
    ->setDefaultContentPagePath('index.php')
    ->setErrorPagePath('index.php')
    ->setPageParameterName(Router::DEFAULT_PAGE_PARAMETER_NAME)
    ->map(Route::newBuilder()
        ->whenRequested('styles.css')
        ->provide('../styles/global.css')
        ->withHeader(Http::HEADER_CONTENT_TYPE_CSS)
        ->build())
    ->map(Route::newBuilder()
        ->whenRequested('global.css.map')
        ->provide('../styles/global.css.map')
        ->build())
    ->map(Route::newBuilder()
        ->whenRequested('img1.png')
        ->provide('../static/img1.png')
        ->withHeader('Content-Type: image/png')
        ->build())
    ->map(Route::newBuilder()
        ->whenRequested('img2.png')
        ->provide('../static/mariadb-setup.png')
        ->withHeader('Content-Type: image/png')
        ->build())
    ->map(Route::newBuilder()
        ->whenRequested('start-server.bat')
        ->provide('../static/start-server.bat')
        ->withHeader('Content-Disposition: attachment; filename="start-server.bat"')
        ->build())
    ->complete();
Templates::setup()->complete();

Router::route();
