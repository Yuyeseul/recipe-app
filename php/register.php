<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$id = isset($_POST["id"]) ? $_POST["id"]:"";
$pw = isset($_POST["pw"]) ? $_POST["pw"]:"";
$name = isset($_POST["name"]) ? $_POST["name"]:"";
$nickname = isset($_POST["nickname"]) ? $_POST["nickname"]:"";
$birth = isset($_POST["birth"]) ? $_POST["birth"]:"";
$phone = isset($_POST["phone"]) ? $_POST["phone"]:"";

$statement = mysqli_prepare($con, "INSERT INTO user VALUES (?,?,?,?,?,?)");
mysqli_stmt_bind_param($statement, "ssssss", $id, $pw, $name, $nickname, $birth, $phone);
mysqli_stmt_execute($statement);

$response = array();
$response["success"] = true;

echo json_encode($response);
?>
