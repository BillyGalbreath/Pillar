<?php

$dir = "/dl/www/";

$val = explode("/", str_replace($dir, "", $_SERVER["REQUEST_URI"]));

$project = @$val[0];
$version = @$val[1];
$build = @$val[2];
$download = @$val[3];

if ($project == null) {
  header('Content-Type: application/json');
  $json = getJson();
  if ($json == null) {
    die('{"error":{"code":404,"message":"Could not locate projects"}}');
  }
  $obj = new stdClass();
  $obj->projects = [];
  foreach($json as $key => $value) {
    array_push($obj->projects, $key);
  }
  sort($obj->projects);
  die(json_encode($obj));
} else if ($version == null) {
  header('Content-Type: application/json');
  $json = @getJson()[$project];
  if ($json == null) {
    die('{"error":{"code":404,"message":"Could not locate project"}}');
  }
  $obj = new stdClass();
  $obj->project = $project;
  $obj->versions = [];
  foreach($json as $key => $value) {
    array_push($obj->versions, $key);
  }
  sort($obj->versions);
  die(json_encode($obj));
} else if ($build == null) {
  header('Content-Type: application/json');
  $json = @getJson()[$project][$version];
  if ($json == null) {
    die('{"error":{"code":404,"message":"Could not locate version"}}');
  }
  $builds = [];
  foreach($json as $key => $value) {
    array_push($builds, "$key");
  }
  rsort($builds);
  $latest = getLatest($json);
  $obj = new stdClass();
  $obj->project = $project;
  $obj->version = $version;
  $obj->builds = new stdClass();
  $obj->builds->latest = "$latest";
  $obj->builds->all = $builds;
  die(json_encode($obj));
} else if ($download == null) {
  header('Content-Type: application/json');
  $json = @getJson()[$project][$version];
  if ($json == null) {
    die('{"error":{"code":404,"message":"Could not locate build"}}');
  }
  if ($build == "latest") {
    $build = getLatest($json);
  }
  $json = @$json[$build];
  if ($json == null) {
    die('{"error":{"code":404,"message":"Could not locate build"}}');
  }
  $obj = new stdClass();
  $obj->project = $project;
  $obj->version = $version;
  $obj->build = "$build";
  $obj = array_merge(json_decode(json_encode($obj), true), $json);
  die(json_encode($obj));
} else {
  header('Content-Type: application/json');
  $json = @getJson()[$project][$version];
  if ($json == null) {
    die('{"error":{"code":404,"message":"Could not locate build"}}');
  }
  if ($build == "latest") {
    $build = getLatest($json);
  }
  $json = @$json[$build];
  if ($json == null) {
    die('{"error":{"code":404,"message":"Could not locate build"}}');
  }
  if ($json["result"] != "SUCCESS") {
    die('{"error":{"code":404,"message":"Build failed. Nothing to download"}}');
  }
  $filename1 = $project . "/" . $version . "/" . $build . "/" . $project . "-" . $version . "-" . $build;
  $filename2 = $project . "-" . $version . "-" . $build . ".jar";
  header('Content-Type: application/jar, true');
  header('Content-Disposition: attachment; filename="' . $filename2 . '"');
  header('Content-Length: ' . filesize($filename1));
  $file = fopen($filename1, 'r');
  fpassthru($file);
  fclose($file);
  die();
}

function getLatest($json) {
  $versions = [];
  foreach($json as $key => $value) {
    array_push($versions, $key);
  }
  rsort($versions);
  foreach($versions as $version) {
    if ($json[$version]["result"] == "SUCCESS") {
      return $version;
    }
  }
}

function getJson() {
  $filename = 'data.json';
  $contents = file_get_contents("../" . $filename);
  return json_decode($contents === false ? '' : $contents, true);
}

?>
