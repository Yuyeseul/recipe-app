<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$id = isset($_POST["id"]) ? $_POST["id"]:"";
$pw = isset($_POST["pw"]) ? $_POST["pw"]:"";

$statement = mysqli_prepare($con, "SELECT * FROM user WHERE id = ? AND pw = ?");
if (!$statement) {
    die('mysqli_prepare() failed: ' . mysqli_error($con));
}

mysqli_stmt_bind_param($statement, "ss", $id, $pw);

if (!mysqli_stmt_execute($statement)) {
    die('mysqli_stmt_execute() failed: ' . mysqli_stmt_error($statement));
}

mysqli_stmt_store_result($statement);

mysqli_stmt_bind_result($statement, $idResult, $pwResult, $name, $nickname, $birth, $phone);

$response = array();
$response["success"] = false;

if (mysqli_stmt_fetch($statement)) {
    $response["success"] = true;
    $response["id"] = $idResult;
    $response["pw"] = $pwResult;
    $response["name"] = $name;
    $response["nickname"] = $nickname;
    $response["birth"] = $birth;
    $response["phone"] = $phone;
}

echo json_encode($response);
?>
