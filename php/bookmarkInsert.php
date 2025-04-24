<?php
$con = mysqli_connect("", "", "", "");
mysqli_query($con, 'SET NAMES utf8');

$nickname = isset($_POST["nickname"])?$_POST["nickname"]:"";
$recipeName = isset($_POST["recipeName"])?$_POST["recipeName"]:"";

$statement = mysqli_prepare($con, "INSERT INTO bookmark VALUES (?,?)");
mysqli_stmt_bind_param($statement, "ss", $nickname, $recipeName);
mysqli_stmt_execute($statement);

$response = array();
$response["success"] = true;


echo json_encode($response);

?>